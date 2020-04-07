package com.example.stayahead.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stayahead.*
import java.util.ArrayList
import java.util.zip.Inflater

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var root: View
    private lateinit var db :DatabaseHelper
    private lateinit var rvList :RecyclerView
    var listItems = ArrayList<Goal>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProviders.of(this).get(HomeViewModel::class.java)
        root = inflater.inflate(R.layout.fragment_home, container, false)
        Log.d("TAG","home!")
        rvList = root.findViewById<RecyclerView>(R.id.rvList)

        listItems = ArrayList<Goal>()

        db = DatabaseHelper(root.context)


        return root
    }


    override fun onStart() {
        super.onStart()
        listItems.clear()
        Log.d("TAG", "HF:: on resume")
        val c = db.getAllGoalData(false)
        while(c.moveToNext()){
            //                                          goal name               percent                    date                          finished
            Log.d("TAG","       c name is : " + c.getString(1) + " : " + c.getString(2) + " : " + c.getString(3) + " : " + c.getString(4))
            val bool = c.getInt(4) > 0
            val newGoal = Goal(c.getString(1),c.getString(2), c.getString(3), bool, c.getInt(0))
            listItems.add(newGoal)
        }
        if(c.count < 1){
            val tvHomeFragment = root.findViewById<TextView>(R.id.tvHomeFragment)
            tvHomeFragment.visibility = View.GONE
        }

        rvList.layoutManager = LinearLayoutManager(root.context, LinearLayoutManager.VERTICAL, false)
        val adapter = GoalAdapter(listItems)
        rvList.adapter = adapter
    }

    fun testDB(){
        //val db = DatabaseHelper(root.context)
        //db.deleteDB()

    }

}
