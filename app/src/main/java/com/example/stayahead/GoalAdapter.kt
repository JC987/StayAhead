package com.example.stayahead

import android.content.Intent
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
            tvRemainingPercent.text = goal.remainingPercentage

            itemView.setOnClickListener{

                Toast.makeText(itemView.context,"item pressed "+ goal.goalName + "  " + goal.remainingPercentage,Toast.LENGTH_SHORT).show()
                val intent = Intent(itemView.context,DetailedGoalActivity::class.java)
                intent.putExtra("goal_name",goal.goalName)
                intent.putExtra("goal_due_date", goal.getDefDateString())
                intent.putExtra("goal_checkpoints",goal.listofCheckpoint)
                intent.putExtra("goal_percent", goal.remainingPercentage)
                itemView.context.startActivity(intent)
            }
        }
    }
}




/*
class GoalAdapter : RecyclerView.Adapter<GoalAdapter.ViewHolder>{

    fun GoalAdapter(mainActivity: MainActivity, goal: List<Goal>){
        context = timelineActivity
        this.tweets = tweets
    }
    //

    inner class ViewHolder(viewItem: View) : RecyclerView.ViewHolder(viewItem) {
       // internal var ivProfileImage: ImageView
        internal var tvGoalName: TextView
        internal var tvRemainingPercent: TextView
     //   internal var tvTimestamp: TextView

        init {
            tvGoalName = viewItem.findViewById(R.id.goalListText1)
            tvRemainingPercent = viewItem.findViewById(R.id.goalListText2)

        }

        fun bind(goal: Goal) {
            tvGoalName.setText((goal.goalName))
            tvRemainingPercent.setText(goal.remainingPrecentage)
          //  Glide.with(context).load(tweet.user.imageUrl).into(ivProfileImage)

        }
    }
}*/