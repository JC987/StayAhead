package com.example.stayahead

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
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


        if(goal.isFinished) {
            tvFinished.text = "Completed"
            isFinished = true
            Log.d("TAG:", "detailed:: goal finished true")
        }
        else
            tvFinished.text = "Ongoing"

        tvPercentage.text = "${goal.remainingPercentage} %"
    }

    private fun loadCurrentGoalData(){

            val goalCursor = db.getGoal(intent.getIntExtra("goal_id",-1))
            goalCursor.moveToFirst()
            goal = Goal(goalCursor.getString(1), goalCursor.getString(2),
                goalCursor.getString(3), goalCursor.getString(4), (goalCursor.getInt(5)==1), intent.getIntExtra("goal_id",-1))

            //clearNotification()

    }

    private fun loadCreatedCheckpoints() {
        val tvParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT,4.6f)
        val tvParams2 = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT,0.2f)
        tvParams.setMargins(0,32,0,32)
        val cbParams = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT,0.2f)
        cbParams.setMargins(0,32,0,32)
        val tbParams = TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT,5f)
        tbParams.setMargins(32,32,32,32)
        val cursor = db.getCheckpointsForGoal(intent.getIntExtra("goal_id",0))
        
        while(cursor.moveToNext()){
            Log.d("TAG","    :" + cursor.count +"   c name is : " + cursor.getString(1) + " : " + cursor.getString(2) + " : " + cursor.getString(3) + " : " + cursor.getString(4))
            //                                  name            date         time           completed       goal id         id
            val currentCheckpoint = Checkpoint(cursor.getString(1),cursor.getString(3),cursor.getString(4), (cursor.getInt(5) > 0), cursor.getInt(2), cursor.getInt(0))
            numOfCheckpoint++

            val tvCheckpointName = TextView(this)
            val tvCheckpointDate = TextView(this)
            val cbCheckpoint = CheckBox(this)
            tvCheckpointDate.textAlignment = View.TEXT_ALIGNMENT_CENTER
            tvCheckpointName.textAlignment = View.TEXT_ALIGNMENT_CENTER
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
                    val sharedPreferences = getSharedPreferences("settings",Context.MODE_PRIVATE)

                    if(numChecked == numOfCheckpoint && sharedPreferences.getInt("auto_complete", 0) == 1)
                        createFinishDialog()
                    updatedCheckpoints[currentCheckpoint.checkpointId] = 1
                }
                else {
                    numChecked--
                    updatedCheckpoints[currentCheckpoint.checkpointId] = 0
                }

                val df = DecimalFormat("0.0")
                goal.remainingPercentage = df.format(((numChecked/numOfCheckpoint) * 100))
                tvPercentage.text = "${goal.remainingPercentage} %"
            }

            
            val tableRow = TableRow(this)
            //tableRow.setBackgroundColor(Color.WHITE)
            tableRow.setBackgroundResource(R.drawable.full_border)
            tableRow.layoutParams = tbParams

            tableRow.addView(tvCheckpointName)
            tableRow.addView(cbCheckpoint)
            tableRow.addView(tvCheckpointDate)

            tableRow.setOnLongClickListener {
                val t = convertTimeTo12Hour(currentCheckpoint.time)
                Toast.makeText(this,"Due on ${goal.date} at $t",Toast.LENGTH_SHORT).show()
                true
            }
            tableRow.setPadding(16,32,16,32)

            tableLayout.addView(tableRow)

        }
    }

    private fun convertTimeTo12Hour(time: String): String{
        val arr = time.split(":")

        var timeOfDay = "AM"
        if(arr[0].toInt() > 11){
            timeOfDay = "PM"
        }
        val df = DecimalFormat("00")

        val hour = if(arr[0] != "12")
            (df.format(arr[0].toInt()).toInt() % 12 ).toString()
        else
            "12"

        val min = df.format(arr[1].toInt()).toString()

        return "$hour : $min $timeOfDay"

    }

    private fun createFinishDialog() {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Finish Goal")
        dialog.setMessage("Do you want to finish this goal?")
        dialog.setPositiveButton("Yes"){ _, _ ->
            finishGoal()
        }
        dialog.setNegativeButton("No"){_, _ ->}

        dialog.create()
        dialog.show()
    }

    private  fun createDeleteDialog(){
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Delete Goal")
        dialog.setMessage("Do you want to delete this goal? Can not be undone!")
        dialog.setPositiveButton("Yes"){ _, _ ->
            deleteGoal()
        }
        dialog.setNegativeButton("No"){ _, _ ->}

        dialog.create()
        dialog.show()
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
            R.id.action_finish -> createFinishDialog()//Toast.makeText(this,"GOAL COMPLETED", Toast.LENGTH_LONG).show()
            R.id.action_delete -> createDeleteDialog()//Toast.makeText(this,"GOAL deleted", Toast.LENGTH_LONG).show()
            R.id.action_edit_goal -> editGoal()//Toast.makeText(this,"GOAL edited", Toast.LENGTH_LONG).show()
        }
        return super.onOptionsItemSelected(item)
    }


    private fun finishGoal(){
        clearAllNotifications()
        cancelAlarmManagers()
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
        clearAllNotifications()
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
        return  PendingIntent.getBroadcast(applicationContext, resultCode, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT)
     }

    private fun cancelAlarmManagers(){
        val pendingIntentGoal = createPendingIntent(goal.goalId, "goal")
        val alarmManagerGoal = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManagerGoal.cancel(pendingIntentGoal)

        updatedCheckpoints.forEach {
            val pendingIntentCheckpoint = createPendingIntent(it.key + 100000,"checkpoint")
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
            Log.d("Alarmreceiver"," detail cp ${intent.getIntExtra("checkpoint_id",0)}")
            nh.getManager().cancel(intent.getIntExtra("checkpoint_id",0))
        }
    }

    private fun clearAllNotifications(){
        val nh = NotificationHelper(this)
        nh.cancelNotification(goal.goalId)

        updatedCheckpoints.forEach {
            Log.d("alarmreceiver", " from detailed clear all noti  ${(it.key+100000)}")
            nh.cancelNotification((it.key + 100000))
        }
        //nh.cancelNotification(intent.getIntExtra("checkpoint_id",0) + 100000)

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
