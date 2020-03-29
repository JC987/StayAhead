package com.example.stayahead

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.*

class DetailedGoalActivity : AppCompatActivity() {
    val TAG: String = "DeatailedGoalActivity"
    //var listView : ListView? = null
    lateinit var linearLayout: LinearLayout
    lateinit var tableLayout: TableLayout

    //@RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detailed_goal)
        val tvGoalName = findViewById<TextView>(R.id.tvGoalNameDetailed)
        val tvDueDate = findViewById<TextView>(R.id.tvGoalDueDateDetailed)
        val tvFinished = findViewById<TextView>(R.id.tvFinishedDetailed)
        val tvPercentage = findViewById<TextView>(R.id.tvPercentageDetailed)

        //linearLayout = findViewById<LinearLayout>(R.id.detailedLinearLayout)
        tableLayout = findViewById<TableLayout>(R.id.detailedTableLayout)
        tvGoalName.text = (intent.getStringExtra("goal_name"))
        tvDueDate.text = "Due: " + intent.getStringExtra("goal_due_date")
        tvFinished.text = if(intent.getBooleanExtra("goal_finished", false))
            "Completed"
        else
            "Ongoing"
        tvPercentage.text = intent.getStringExtra("goal_percent")

       // var t = DateTimeFormatter.ISO_INSTANT.format(Instant.now())


        createCheckpoints()
    }

    private fun createCheckpoints() {
        //val list = intent.getParcelableArrayListExtra<Checkpoint>("goal_checkpoints")
        //var tvParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT)
        var tvParams = TableRow.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT,4.5f)
        var cbParams = TableRow.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT,0.5f)
        val db = DatabaseHelper(this)
        val c = db.getCheckpointsForGoal(intent.getIntExtra("goal_id",0))


        while(c.moveToNext()){
            Log.d("TAG","    :" + c.count +"   c name is : " + c.getString(1) + " : " + c.getString(2) + " : " + c.getString(3) + " : " + c.getString(4))
            val ckName = c.getString(1)
            val ckCompleted = c.getInt(4) > 0

            //Log.d(TAG,"createCheckpoints: "+ item.checkpointName)
            var newTextView = TextView(this)
            var newCheckBox = CheckBox(this)
            newTextView.layoutParams = tvParams

            newTextView.text = ckName
            newTextView.setTextColor(Color.BLACK)
            newTextView.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            newTextView.setTextSize(18f)

            newCheckBox.layoutParams = cbParams
            newCheckBox.isChecked = ckCompleted

            var tableRow = TableRow(this)
            tableRow.addView(newTextView)
            tableRow.addView(newCheckBox)
            tableRow.setPadding(8,16,8,16)

            tableLayout.addView(tableRow)

        }
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.detailed_goal_menu, menu)
        return true
    }
}
