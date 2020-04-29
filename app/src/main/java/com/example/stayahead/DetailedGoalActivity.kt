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
    lateinit var linearLayout: LinearLayout
    lateinit var tableLayout: TableLayout
    lateinit  var tvPercentage: TextView
    val map = mutableMapOf<Int, Int>()
    var goalPercent:String = ""
    var goalId = -1
    var goalDate:String = ""
    var goalName = ""
    var numOfCheckpoint: Float = 0f
    var numChecked: Float = 0f
    var isFinished = false
    val db = DatabaseHelper(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detailed_goal)
        val tvGoalName = findViewById<TextView>(R.id.tvGoalNameDetailed)
        val tvDueDate = findViewById<TextView>(R.id.tvGoalDueDateDetailed)
        val tvFinished = findViewById<TextView>(R.id.tvFinishedDetailed)
        tvPercentage = findViewById<TextView>(R.id.tvPercentageDetailed)

        goalId = intent.getIntExtra("goal_id",-1)

        if(intent.getIntExtra("is_notification",0) == 0){
            Log.d("NotificationHelper"," shouldnt be a notificaion")
            goalName = intent.getStringExtra("goal_name")
            goalDate = intent.getStringExtra("goal_due_date")
            goalPercent = intent.getStringExtra("goal_percent")
        }
        else{
            val goalC = db.getGoal(goalId)
            goalC.moveToFirst()
            goalName = goalC.getString(1)
            goalDate = goalC.getString(3)
            goalPercent = goalC.getString(2)

            //clear that notification
            if(intent.getIntExtra("is_checkpoint",0) == 0) {
                val nh = NotificationHelper(this)
                nh.cancelNotification(goalId)
            }
            else{
                val nh = NotificationHelper(this)
                nh.cancelNotification(intent.getIntExtra("checkpoint_id",0) + 100000)
            }
        }
        //linearLayout = findViewById<LinearLayout>(R.id.detailedLinearLayout)
        tableLayout = findViewById<TableLayout>(R.id.detailedTableLayout)
       // goalName = intent.getStringExtra("goal_name")
        tvGoalName.text = (goalName)
        //goalDate = intent.getStringExtra("goal_due_date")
        tvDueDate.text = "Due: $goalDate"
        if(intent.getBooleanExtra("goal_finished", false)) {
            tvFinished.text = "Completed"
            isFinished = true
        }
        else
            tvFinished.text = "Ongoing"

        //goalPercent = intent.getStringExtra("goal_percent")
        tvPercentage.text = "$goalPercent %"


        createCheckpoints()
    }

    private fun createCheckpoints() {
        val tvParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT,4.6f)
        val tvParams2 = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT,0.2f)
        tvParams.marginStart = 32
        tvParams.marginEnd = 32
        val cbParams = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT,0.2f)
        val tbParams = TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT,5f)
        //get all cps for this goal

        val c = db.getCheckpointsForGoal(intent.getIntExtra("goal_id",0))
        
        while(c.moveToNext()){
            Log.d("TAG","    :" + c.count +"   c name is : " + c.getString(1) + " : " + c.getString(2) + " : " + c.getString(3) + " : " + c.getString(4))
            //                                  name            date         time           completed       goal id         id
            val currentCheckpoint = Checkpoint(c.getString(1),c.getString(3),"time", (c.getInt(4) > 0), c.getInt(2), c.getInt(0))
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
           // tvCheckpointName.width = 200

                        tvCheckpointDate.text = "Due:\n${currentCheckpoint.date}"
                        tvCheckpointDate.setTextColor(Color.BLACK)
                        tvCheckpointDate.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                        tvCheckpointDate.textSize = 18f
            // tvCheckpointDate.minWidth = 400

            cbCheckpoint.layoutParams = cbParams
            cbCheckpoint.isChecked = currentCheckpoint.isCompleted

            if(currentCheckpoint.isCompleted) {
                numChecked++
                map[currentCheckpoint.checkpointId] = 1
            }
            else
                map[currentCheckpoint.checkpointId] = 0


            cbCheckpoint.setOnClickListener {
                if(cbCheckpoint.isChecked) {
                    numChecked++
                    map[currentCheckpoint.checkpointId] = 1
                }
                else {
                    numChecked--
                    map[currentCheckpoint.checkpointId] = 0
                }

                //tvPercentage.text = ((numChecked/numOfCheckpoint) * 100).toString()
                val df = DecimalFormat("#.0")
                goalPercent = df.format(((numChecked/numOfCheckpoint) * 100))
                tvPercentage.text = "${goalPercent} %"
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
        // Inflate the menu; this adds items to the action bar if it is present.
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

    fun finishGoal(){
        if(goalId == -1 || isFinished)
            return
        db.finishGoal(goalId)
        db.close()
        Toast.makeText(this,"Goal Finished",Toast.LENGTH_SHORT).show()
        val i = Intent(this, SideNavDrawer::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(i)
    }

    private fun createPendingIntent(resultCode:Int, type:String) : PendingIntent {
        val notifyIntent = Intent(this, AlarmReceiver::class.java)
        notifyIntent.putExtra("goal_name",goalName)
        notifyIntent.putExtra("type",type)
        notifyIntent.putExtra("code",resultCode)
        notifyIntent.putExtra("goal_id", goalId)
        return  PendingIntent.getBroadcast(this, resultCode, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT)
     }

    fun deleteGoal(){
        val pendingIntent = createPendingIntent(goalId, "goal")
        val am = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        am.cancel(pendingIntent)

        map.forEach {

            val pendingIntent2 = createPendingIntent(it.key,"checkpoint")
            val am2 = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            am2.cancel(pendingIntent2)
        }

        if(goalId == -1)
            return
        Toast.makeText(this,"Goal Deleted",Toast.LENGTH_SHORT).show()
        db.deleteGoal(goalId)
        db.close()

        val i = Intent(this, SideNavDrawer::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(i)
    }
    fun editGoal(){
        val i = Intent(this, EditGoalActivity::class.java)
        //i.putExtra("goal_name",goalName)
        i.putExtra("goal_id", goalId)
        startActivity(i)
    }


    override fun onPause() {
        Log.d("TAG", "on PAUSED called")
        map.forEach{
            db.updateCheckpointCompleted(it.key,it.value)
        }
        db.updateGoalPercentage(goalId,goalPercent)
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
    fun convertDateToStandardView(date:String) : String{

        var s = date.substring(4)
        s += date.substring(0,4)

        return s
    }
}
