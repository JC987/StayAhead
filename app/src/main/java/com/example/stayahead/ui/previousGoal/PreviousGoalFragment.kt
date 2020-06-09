package com.example.stayahead.ui.previousGoal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stayahead.DatabaseHelper
import com.example.stayahead.Goal
import com.example.stayahead.GoalAdapter
import com.example.stayahead.R

class PreviousGoalFragment : Fragment() {

    lateinit var db:DatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_previous_goals, container, false)
        val rv = root.findViewById<RecyclerView>(R.id.rvPreviousGoalFragment)

        db = DatabaseHelper(root.context)
        val c = db.getFinishedGoals()
        if(c.count < 1){
            val tvPreviousGoalFragment: TextView = root.findViewById(R.id.tvPreviousGoalFragment)
            tvPreviousGoalFragment.visibility = View.VISIBLE
        }
        val listOfGoals = ArrayList<Goal>()
        while(c.moveToNext()){
            val bool = c.getInt(4) > 0
            val newGoal = Goal(c.getString(1),c.getString(2), c.getString(3), c.getString(4), bool, c.getInt(0))
            listOfGoals.add(newGoal)
        }

        val adapter = GoalAdapter(listOfGoals)

        rv.layoutManager = LinearLayoutManager(root.context,LinearLayoutManager.VERTICAL,false)
        rv.adapter = adapter

        return root
    }
}