package com.example.stayahead.ui.home

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stayahead.*
import java.util.*
import java.util.Calendar.*

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var root: View
    private lateinit var db :DatabaseHelper
    private lateinit var rvList :RecyclerView
    private lateinit var sharedPreferences:SharedPreferences
    var listItems = ArrayList<Goal>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProviders.of(this).get(HomeViewModel::class.java)
        root = inflater.inflate(R.layout.fragment_home, container, false)

        //set action bar title
        (activity as SideNavDrawer).supportActionBar?.title = "Stay Ahead!"
        //allows for option menu
        setHasOptionsMenu(true)
        sharedPreferences = root.context.getSharedPreferences("settings",Context.MODE_PRIVATE)
        Log.d("TAG","home!")
        rvList = root.findViewById<RecyclerView>(R.id.rvList)

        listItems = ArrayList<Goal>()

        db = DatabaseHelper(root.context)



        return root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_home_sort_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.action_sort_oldest -> resetList(item.order - 1)
            R.id.action_sort_newest -> resetList(item.order - 1)
            R.id.action_sort_upcoming -> resetList(item.order - 1)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        val getKey = sharedPreferences.getInt("home_sort_key", 0)
        resetList(getKey)
    }

    private fun resetList(sortKey:Int) {
        listItems.clear()
        Log.d("TAG", "HF:: on resume")
        lateinit var cursor:Cursor
        when(sortKey){
            0 -> cursor = db.getActiveGoalsData(false)
            1 -> cursor = db.getActiveGoalsData(true)
            2 -> cursor = db.getActiveGoalsDataByDate()
        }

        val editor:SharedPreferences.Editor = sharedPreferences.edit()
        editor.putInt("home_sort_key",sortKey)
        editor.apply()

        while(cursor.moveToNext()){
            //                                          goal name               percent                    date                          finished
            Log.d("TAG","       c name is : " + cursor.getString(1) + " : " + cursor.getString(2) + " : " + cursor.getString(3) + " : " + cursor.getString(4) + " : key is "+ sortKey)
            val bool = cursor.getInt(5) > 0
            val newGoal = Goal(cursor.getString(1),cursor.getString(2), cursor.getString(3), cursor.getString(4), bool, cursor.getInt(0))
            listItems.add(newGoal)
        }
        if(cursor.count < 1){
            val tvHomeFragment = root.findViewById<TextView>(R.id.tvHomeFragment)
            tvHomeFragment.visibility = View.VISIBLE
        }

        rvList.layoutManager = LinearLayoutManager(root.context, LinearLayoutManager.VERTICAL, false)
        val adapter = GoalAdapter(listItems)
        rvList.adapter = adapter

        db.close()
    }


}
