package com.example.stayahead.ui.previousGoal

import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.view.*
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
    lateinit var root:View
    lateinit var rv:RecyclerView
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_previous_goals, container, false)
        rv = root.findViewById<RecyclerView>(R.id.rvPreviousGoalFragment)
        setHasOptionsMenu(true)

        db = DatabaseHelper(root.context)

        var sharedPreferences = root.context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val getKey = sharedPreferences.getInt("home_sort_key", 0)
        resetList(getKey)
        return root
    }

    private fun resetList(sortKey:Int){
        lateinit var c: Cursor
        when(sortKey){
            0 -> c = db.getFinishedGoals(false)
            1 -> c = db.getFinishedGoals(true)
            2 -> c = db.getFinishedGoalsDataByDate()
        }
            if (c.count < 1) {
                val tvPreviousGoalFragment: TextView =
                    root.findViewById(R.id.tvPreviousGoalFragment)
                tvPreviousGoalFragment.visibility = View.VISIBLE
            }
            val listOfGoals = ArrayList<Goal>()
            while (c.moveToNext()) {
                val bool = c.getInt(4) > 0
                val newGoal = Goal(
                    c.getString(1),
                    c.getString(2),
                    c.getString(3),
                    c.getString(4),
                    bool,
                    c.getInt(0)
                )
                listOfGoals.add(newGoal)
            }

            val adapter = GoalAdapter(listOfGoals)

            rv.layoutManager =
                LinearLayoutManager(root.context, LinearLayoutManager.VERTICAL, false)
            rv.adapter = adapter


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
}