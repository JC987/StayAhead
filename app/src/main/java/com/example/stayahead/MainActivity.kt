package com.example.stayahead

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.ArrayList

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val rvList = findViewById<RecyclerView>(R.id.rvList)
        val listItems = ArrayList<Goal>()
        var cpList = ArrayList<Checkpoint>()
        cpList.add(Checkpoint("first",true))
        cpList.add(Checkpoint("second", false))
        cpList.add(Checkpoint("first",true))
        cpList.add(Checkpoint("second", false))
        cpList.add(Checkpoint("first",true))
        cpList.add(Checkpoint("second", false))
        cpList.add(Checkpoint("first",true))
        cpList.add(Checkpoint("seconddxsxfdgcjkn", false))
        cpList.add(Checkpoint("second", false))
        cpList.add(Checkpoint("first",true))
        cpList.add(Checkpoint("seconddxsxfdgcjkn", false))
        val goal = Goal("test goal", "99%", cpList)
        listItems.add(goal)
        listItems.add(goal)
        cpList.add(Checkpoint("new third",false))
        listItems.add(Goal("hello","15%", cpList))
        listItems.add(goal)
        listItems.add(goal)
        //val adapter = ArrayAdapter(this,R.layout.active_goal_list_item, R.id.goalListText1, listItems)
        rvList.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL, false)
        var adapter = GoalAdapter(listItems)
        rvList.adapter = adapter


    }
}
