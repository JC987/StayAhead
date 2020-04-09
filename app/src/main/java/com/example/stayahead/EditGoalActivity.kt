package com.example.stayahead

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import kotlinx.android.synthetic.main.activity_edit_goal.*

class EditGoalActivity : AppCompatActivity() {
    var goalId = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_create_new_goal)
        goalId = intent.getIntExtra("goal_id",1)
        val db = DatabaseHelper(this)
        val etGoalName = findViewById<EditText>(R.id.etGoalName)
        val tvGoalDate = findViewById<TextView>(R.id.tvDueDate)
        val btnDate = findViewById<Button>(R.id.btnPickDueDate)
        val btnAddCheckpoint = findViewById<Button>(R.id.btnAddCheckpoint)
        val tableLayout = findViewById<TableLayout>(R.id.lvCheckpoints)
        val btnSubmit = findViewById<Button>(R.id.btnSubmitNewGoal)

        val c = db.getGoal(goalId)
        c.moveToNext()
        Log.d("TAG","count is " + c.count)
        Log.d("TAG",c.getString(1))
        etGoalName.setText(c.getString(1))
        tvGoalDate.text = c.getString(3)

        val c2 = db.getAllCheckpointsOfGoal(goalId)
        while(c2.moveToNext()){

            val tableRow = TableRow(this)
            val trParams = TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,TableLayout.LayoutParams.WRAP_CONTENT)

            val etParams = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 10f);
            val editText = EditText(this)

            val btnDateCk = Button(this)
            val btnParams = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1f);

            tableRow.layoutParams = trParams
            editText.layoutParams = etParams
            btnDateCk.layoutParams = btnParams

            editText.setText(c2.getString(1))
            btnDateCk.text = c2.getString(3)

            tableRow.addView(editText)
            tableRow.addView(btnDateCk)
            tableLayout.addView(tableRow)
        }
    }
}
