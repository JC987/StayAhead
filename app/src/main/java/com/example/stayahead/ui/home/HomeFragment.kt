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
        //activity?.title = "Stay Ahead!"
        (activity as SideNavDrawer).supportActionBar?.title = "Stay Ahead!"
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

    private fun resetList(key:Int) {
        listItems.clear()
        Log.d("TAG", "HF:: on resume")
        lateinit var c:Cursor
        when(key){
            0 -> c = db.getAllGoalData(false)
            1 -> c = db.getAllGoalData(true)
            2 -> c = db.getAllGoalDataByDate()
        }




        val editor:SharedPreferences.Editor = sharedPreferences.edit()
        editor.putInt("home_sort_key",key)
        editor.apply()

        while(c.moveToNext()){
            //                                          goal name               percent                    date                          finished
            Log.d("TAG","       c name is : " + c.getString(1) + " : " + c.getString(2) + " : " + c.getString(3) + " : " + c.getString(4) + " : key is "+ key)
            val bool = c.getInt(4) > 0
            val newGoal = Goal(c.getString(1),c.getString(2), c.getString(3), bool, c.getInt(0))
            listItems.add(newGoal)
        }
        if(c.count < 1){
            val tvHomeFragment = root.findViewById<TextView>(R.id.tvHomeFragment)
            tvHomeFragment.visibility = View.VISIBLE
        }

        rvList.layoutManager = LinearLayoutManager(root.context, LinearLayoutManager.VERTICAL, false)
        val adapter = GoalAdapter(listItems)
        rvList.adapter = adapter
    }


}
