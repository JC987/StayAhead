package com.example.stayahead

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class GoalAdapter(val goalList: ArrayList<Goal>) : RecyclerView.Adapter<GoalAdapter.ViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.active_goal_list_item, parent, false)
        return ViewHolder(layout)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(goalList[position])
    }

    override fun getItemCount(): Int {
        return goalList.size
    }
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(goal: Goal) {
            val tvGoalName = itemView.findViewById(R.id.goalListText1) as TextView
            val tvRemainingPercent  = itemView.findViewById(R.id.goalListText2) as TextView
            tvGoalName.text = goal.goalName
            tvRemainingPercent.text = goal.remainingPercentage + "%"

            itemView.setOnClickListener{

                val intent = Intent(itemView.context,DetailedGoalActivity::class.java)

                intent.putExtra("goal_name",goal.goalName)
                intent.putExtra("goal_due_date", goal.getDateAsString())
                intent.putExtra("goal_checkpoints",goal.getList())
                intent.putExtra("goal_percent", goal.remainingPercentage)
                intent.putExtra("goal_finished", goal.isFinished)
                intent.putExtra("goal_id", goal.goalId)
                itemView.context.startActivity(intent)
            }
        }
    }
}



