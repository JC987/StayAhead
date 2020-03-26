package com.example.stayahead.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProviders.of(this).get(HomeViewModel::class.java)
        root = inflater.inflate(R.layout.fragment_home, container, false)
        Log.d("TAG","home!")
        val rvList = root.findViewById<RecyclerView>(R.id.rvList)
       /* homeViewModel.text.observe(this, Observer {
            textView.text = it
        })*/
       // testDB()
        val listItems = ArrayList<Goal>()
        var cpList = ArrayList<Checkpoint>()

        val db = DatabaseHelper(root.context)

        val c = db.getData(false)
        while(c.moveToNext()){
            Log.d("TAG","       c name is : " + c.getString(1) + " : " + c.getString(2) + " : " + c.getString(3) + " : " + c.getString(4))
            val bool = c.getInt(4) > 0
            val newGoal = Goal(c.getString(1),c.getString(2), bool)
            listItems.add(newGoal)
        }

        /*  cpList.add(Checkpoint("first", "asdf", "zxcv",true))
        cpList.add(Checkpoint("second","asdf", "zxcv", false))
        cpList.add(Checkpoint("third", "asdf", "zxcv",false))
        val goal = Goal("goal", "99%", false)
        for(cp in cpList){

            goal.addCheckpoint(cp)
        }
        listItems.add(goal)
        listItems.add(goal)
        listItems.add(goal)
        listItems.add(goal)*/
        //val adapter = ArrayAdapter(this,R.layout.active_goal_list_item, R.id.goalListText1, listItems)
        rvList.layoutManager = LinearLayoutManager(root.context, LinearLayoutManager.VERTICAL, false)
        var adapter = GoalAdapter(listItems)
        rvList.adapter = adapter
        return root
    }

    fun testDB(){
        val db = DatabaseHelper(root.context)
        //db.deleteDB()
       // db.deleteDB()
        val goal = Goal("test goal", "99%", false )
        val goal2 = Goal("cooper sucks", "15%", true )
        db.addData(goal)
        db.addData(goal2)


    }

}
