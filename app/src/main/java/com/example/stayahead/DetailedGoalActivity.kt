package com.example.stayahead

import android.app.Activity
import android.app.ActivityManager
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.core.view.marginLeft
import androidx.core.view.marginStart
import com.example.stayahead.ui.home.HomeFragment
import java.text.DecimalFormat

class DetailedGoalActivity : AppCompatActivity() {
    val TAG: String = "DeatailedGoalActivity"
    lateinit var linearLayout: LinearLayout
    lateinit var tableLayout: TableLayout
    lateinit  var tvPercentage: TextView
    val map = mutableMapOf<Int, Int>()
    var goalPercent = ""
    var numOfCheckpoint: Float = 0f
    var numChecked: Float = 0f
    val db = DatabaseHelper(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detailed_goal)
        val tvGoalName = findViewById<TextView>(R.id.tvGoalNameDetailed)
        val tvDueDate = findViewById<TextView>(R.id.tvGoalDueDateDetailed)
        val tvFinished = findViewById<TextView>(R.id.tvFinishedDetailed)
        tvPercentage = findViewById<TextView>(R.id.tvPercentageDetailed)

        //linearLayout = findViewById<LinearLayout>(R.id.detailedLinearLayout)
        tableLayout = findViewById<TableLayout>(R.id.detailedTableLayout)
        tvGoalName.text = (intent.getStringExtra("goal_name"))
        tvDueDate.text = "Due: " + intent.getStringExtra("goal_due_date")
        tvFinished.text = if(intent.getBooleanExtra("goal_finished", false))
            "Completed"
        else
            "Ongoing"

        goalPercent = intent.getStringExtra("goal_percent")
        tvPercentage.text = "$goalPercent %"

       // var t = DateTimeFormatter.ISO_INSTANT.format(Instant.now())


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
            tvCheckpointName.layoutParams = tvParams
            tvCheckpointDate.layoutParams = tvParams2


            tvCheckpointName.text = "${currentCheckpoint.checkpointName}"
            tvCheckpointName.setTextColor(Color.BLACK)
            tvCheckpointName.textAlignment = TextView.TEXT_ALIGNMENT_TEXT_START
            tvCheckpointName.textSize = 18f
           // tvCheckpointName.width = 200

                        tvCheckpointDate.text = "Due on: \n${currentCheckpoint.date}"
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
                val df = DecimalFormat("#.##")
                goalPercent = df.format(((numChecked/numOfCheckpoint) * 100))
                tvPercentage.text = "${goalPercent} %"
            }

            
            val tableRow = TableRow(this)
            tableRow.layoutParams = tbParams

            tableRow.addView(tvCheckpointDate)
            tableRow.addView(tvCheckpointName)
            tableRow.addView(cbCheckpoint)

            tableRow.setPadding(16,32,16,32)

            tableLayout.addView(tableRow)

        }
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.detailed_goal_menu, menu)
        return true
    }

    override fun onPause() {
        Log.d("TAG", "on PAUSED called")
        map.forEach{
            db.updateCheckpointCompleted(it.key,it.value)
        }
        db.updateGoalPercentage(intent.getIntExtra("goal_id",0),goalPercent)
        super.onPause()

    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun finish() {
        Log.d("TAG", "on finish called")
        super.finish()

    }
}
