package com.example.stayahead.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stayahead.Checkpoint
import com.example.stayahead.Goal
import com.example.stayahead.R
import com.example.stayahead.GoalAdapter
import java.util.ArrayList

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        val rvList = root.findViewById<RecyclerView>(R.id.rvList)
       /* homeViewModel.text.observe(this, Observer {
            textView.text = it
        })*/

        val listItems = ArrayList<Goal>()
        var cpList = ArrayList<Checkpoint>()
        cpList.add(Checkpoint("first", "asdf", "zxcv",true))
        cpList.add(Checkpoint("second","asdf", "zxcv", false))
        cpList.add(Checkpoint("third", "asdf", "zxcv",false))
        val goal = Goal("test goal", "99%", cpList)
        listItems.add(goal)
        listItems.add(goal)
        listItems.add(Goal("hello","15%", cpList))
        listItems.add(goal)
        listItems.add(goal)
        //val adapter = ArrayAdapter(this,R.layout.active_goal_list_item, R.id.goalListText1, listItems)
        rvList.layoutManager = LinearLayoutManager(root.context, LinearLayoutManager.VERTICAL, false)
        var adapter = GoalAdapter(listItems)
        rvList.adapter = adapter
        return root
    }


}