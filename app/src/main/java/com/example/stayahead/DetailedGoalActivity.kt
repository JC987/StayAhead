package com.example.stayahead

import android.app.Activity
import android.app.ActivityManager
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.core.view.marginLeft
import androidx.core.view.marginStart
import com.example.stayahead.ui.createNewGoal.CreateNewGoalFragment
import com.example.stayahead.ui.home.HomeFragment
import java.text.DecimalFormat

class DetailedGoalActivity : AppCompatActivity() {
    val TAG: String = "DeatailedGoalActivity"
    lateinit var tableLayout: TableLayout
    lateinit  var tvPercentage: TextView
    private val updatedCheckpoints = mutableMapOf<Int, Int>()
    private lateinit var goal:Goal

    private var numOfCheckpoint: Float = 0f
    private var numChecked: Float = 0f
    private var isFinished = false
    private val db = DatabaseHelper(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detailed_goal)

        loadCurrentGoalData()
        loadViewData()
        loadCreatedCheckpoints()
    }

    private fun loadViewData(){
        val tvGoalName = findViewById<TextView>(R.id.tvGoalNameDetailed)
        val tvDueDate = findViewById<TextView>(R.id.tvGoalDueDateDetailed)
        val tvFinished = findViewById<TextView>(R.id.tvFinishedDetailed)
        tvPercentage = findViewById<TextView>(R.id.tvPercentageDetailed)
        tableLayout = findViewById<TableLayout>(R.id.detailedTableLayout)


        tvGoalName.text = (goal.goalName)
        tvDueDate.text = "Due: ${goal.date}"
        if(intent.getBooleanExtra("goal_finished", false)) {
            tvFinished.text = "Completed"
            isFinished = true
        }
        else
            tvFinished.text = "Ongoing"

        tvPercentage.text = "${goal.remainingPercentage} %"
    }

    private fun loadCurrentGoalData(){
        //goalId = intent.getIntExtra("goal_id",-1)

        if(intent.getIntExtra("is_notification",0) == 0){
            goal = Goal(intent.getStringExtra("goal_name"), intent.getStringExtra("goal_percent"),
                intent.getStringExtra("goal_due_date"),false, intent.getIntExtra("goal_id",-1))
            Log.d(TAG,"Not coming from a notification")

        }
        else{
            val goalCursor = db.getGoal(intent.getIntExtra("goal_id",-1))
            goalCursor.moveToFirst()
            goal = Goal(goalCursor.getString(1), goalCursor.getString(2),
                goalCursor.getString(3),false, intent.getIntExtra("goal_id",-1))

            clearNotification()
        }
    }

    private fun loadCreatedCheckpoints() {
        val tvParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT,4.6f)
        val tvParams2 = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT,0.2f)
        tvParams.marginStart = 32
        tvParams.marginEnd = 32
        val cbParams = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT,0.2f)
        val tbParams = TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT,5f)

        val cursor = db.getCheckpointsForGoal(intent.getIntExtra("goal_id",0))
        
        while(cursor.moveToNext()){
            Log.d("TAG","    :" + cursor.count +"   c name is : " + cursor.getString(1) + " : " + cursor.getString(2) + " : " + cursor.getString(3) + " : " + cursor.getString(4))
            //                                  name            date         time           completed       goal id         id
            val currentCheckpoint = Checkpoint(cursor.getString(1),cursor.getString(3),"time", (cursor.getInt(4) > 0), cursor.getInt(2), cursor.getInt(0))
            numOfCheckpoint++

            val tvCheckpointName = TextView(this)
            val tvCheckpointDate = TextView(this)
            val cbCheckpoint = CheckBox(this)

            if(isFinished)
                cbCheckpoint.isEnabled = false

            tvCheckpointName.layoutParams = tvParams
            tvCheckpointDate.layoutParams = tvParams2


            tvCheckpointName.text = "${currentCheckpoint.checkpointName}"
            tvCheckpointName.setTextColor(Color.BLACK)
            tvCheckpointName.textAlignment = TextView.TEXT_ALIGNMENT_TEXT_START
            tvCheckpointName.textSize = 18f

            tvCheckpointDate.text = "Due:\n${currentCheckpoint.date}"
            tvCheckpointDate.setTextColor(Color.BLACK)
            tvCheckpointDate.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            tvCheckpointDate.textSize = 18f

            cbCheckpoint.layoutParams = cbParams
            cbCheckpoint.isChecked = currentCheckpoint.isCompleted

            if(currentCheckpoint.isCompleted) {
                numChecked++
                updatedCheckpoints[currentCheckpoint.checkpointId] = 1
            }
            else
                updatedCheckpoints[currentCheckpoint.checkpointId] = 0


            cbCheckpoint.setOnClickListener {
                if(cbCheckpoint.isChecked) {
                    numChecked++
                    updatedCheckpoints[currentCheckpoint.checkpointId] = 1
                }
                else {
                    numChecked--
                    updatedCheckpoints[currentCheckpoint.checkpointId] = 0
                }

                val df = DecimalFormat("#.0")
                goal.remainingPercentage = df.format(((numChecked/numOfCheckpoint) * 100))
                tvPercentage.text = "${goal.remainingPercentage} %"
            }

            
            val tableRow = TableRow(this)
            tableRow.layoutParams = tbParams

            tableRow.addView(tvCheckpointName)
            tableRow.addView(cbCheckpoint)
            tableRow.addView(tvCheckpointDate)

            tableRow.setPadding(16,32,16,32)

            tableLayout.addView(tableRow)

        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.detailed_goal_menu, menu)
        if(isFinished) {
            menu.findItem(R.id.action_edit_goal).isVisible = false
            menu.findItem(R.id.action_finish).isVisible = false
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_finish -> finishGoal()//Toast.makeText(this,"GOAL COMPLETED", Toast.LENGTH_LONG).show()
            R.id.action_delete -> deleteGoal()//Toast.makeText(this,"GOAL deleted", Toast.LENGTH_LONG).show()
            R.id.action_edit_goal -> editGoal()//Toast.makeText(this,"GOAL edited", Toast.LENGTH_LONG).show()
        }
        return super.onOptionsItemSelected(item)
    }


    private fun finishGoal(){
        if(goal.goalId == -1 || isFinished)
            return
        db.finishGoal(goal.goalId)
        db.close()
        Toast.makeText(this,"Goal Finished",Toast.LENGTH_SHORT).show()
        val i = Intent(this, SideNavDrawer::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(i)
    }

    private fun deleteGoal(){
        cancelAlarmManagers()
        if(goal.goalId == -1)
            return
        Toast.makeText(this,"Goal Deleted",Toast.LENGTH_SHORT).show()
        db.deleteGoal(goal.goalId)
        db.close()

        val i = Intent(this, SideNavDrawer::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(i)
    }

    private fun editGoal(){
        val i = Intent(this, EditGoalActivity::class.java)
        i.putExtra("goal_id", goal.goalId)
        startActivity(i)
    }


    private fun createPendingIntent(resultCode:Int, type:String) : PendingIntent {
        val notifyIntent = Intent(this, AlarmReceiver::class.java)
        notifyIntent.putExtra("goal_name",goal.goalName)
        notifyIntent.putExtra("type",type)
        notifyIntent.putExtra("code",resultCode)
        notifyIntent.putExtra("goal_id", goal.goalId)
        return  PendingIntent.getBroadcast(this, resultCode, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT)
     }

    private fun cancelAlarmManagers(){
        val pendingIntentGoal = createPendingIntent(goal.goalId, "goal")
        val alarmManagerGoal = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManagerGoal.cancel(pendingIntentGoal)

        updatedCheckpoints.forEach {
            val pendingIntentCheckpoint = createPendingIntent(it.key,"checkpoint")
            val alarmManagerCheckpoint = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManagerCheckpoint.cancel(pendingIntentCheckpoint)
        }
    }

    private fun clearNotification(){
        val nh = NotificationHelper(this)
        if(intent.getIntExtra("is_checkpoint",0) == 0) {
            nh.cancelNotification(goal.goalId)
        }
        else{
            nh.cancelNotification(intent.getIntExtra("checkpoint_id",0) + 100000)
        }
    }


    override fun onPause() {
        Log.d("TAG", "on PAUSED called")
        updatedCheckpoints.forEach{
            db.updateCheckpointCompleted(it.key,it.value)
        }
        db.updateGoalPercentage(goal.goalId,goal.remainingPercentage)
        super.onPause()

    }

    override fun onBackPressed() {
        if(intent.getIntExtra("is_notification",0) == 1) {
            val i = Intent(this, SideNavDrawer::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(i)
        }
        super.onBackPressed()
    }

    override fun finish() {
        Log.d("TAG", "on finish called")
        super.finish()

    }

}
