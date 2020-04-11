package com.example.stayahead

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.view.get
import kotlinx.android.synthetic.main.activity_edit_goal.*
import kotlinx.android.synthetic.main.fragment_create_new_goal.*
import java.text.DecimalFormat

class EditGoalActivity : AppCompatActivity() {
    var goalId = -1
    lateinit var  tableLayout :TableLayout
    private lateinit var newGoal: Goal
    private  var oldList: ArrayList<Checkpoint> = ArrayList()
    val mapOld = mutableMapOf<Int, TableRow>()
    private  var newList: ArrayList<TableRow> = ArrayList()
    private lateinit var etGoalName: EditText
    var date = ""
    var totalCheckpoints:Float = 0f
    var totalCheckpointsCompleted:Float = 0f
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_create_new_goal)
        goalId = intent.getIntExtra("goal_id",1)
        val db = DatabaseHelper(this)
        etGoalName = findViewById<EditText>(R.id.etGoalName)
        val tvGoalDate = findViewById<TextView>(R.id.tvDueDate)
        val btnDate = findViewById<Button>(R.id.btnPickDueDate)
        val btnAddCheckpoint = findViewById<Button>(R.id.btnAddCheckpoint)
         tableLayout = findViewById<TableLayout>(R.id.lvCheckpoints)
        val btnSubmit = findViewById<Button>(R.id.btnSubmitNewGoal)

        val c = db.getGoal(goalId)
        c.moveToNext()
        Log.d("TAG","count is " + c.count)
        Log.d("TAG",c.getString(1))
        etGoalName.setText(c.getString(1))
        date = c.getString(3)
        tvGoalDate.text = date
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

            btnDateCk.setOnClickListener{
                datePickerDialog(btnDateCk)
            }

            tableRow.addView(editText)
            tableRow.addView(btnDateCk)
            tableLayout.addView(tableRow)

            mapOld[c2.getInt(0)] = tableRow//same as .put

            if(c2.getInt(4) == 1){
                totalCheckpointsCompleted+=1
            }
            totalCheckpoints+=1
        }

        btnDate.setOnClickListener {
            datePickerDialog(btnDate)
        }
        btnSubmit.setOnClickListener{
            submit()
        }
        btnAddCheckpoint.setOnClickListener{
            createCheckpoint()
        }
    }

    fun createCheckpoint(){
        val tableRow = TableRow(this)
        val trParams = TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,TableLayout.LayoutParams.WRAP_CONTENT)

        val etParams = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 10f);
        val editText = EditText(this)

        val btnDateCk = Button(this)
        val btnParams = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1f);


        btnDateCk.setOnClickListener {
            datePickerDialog(btnDateCk)
        }

        tableRow.layoutParams = trParams

        btnDateCk.text = "Date"

        btnDateCk.layoutParams = btnParams

        editText.layoutParams = etParams
        editText.hint = "Enter a checkpoint!"
        editText.setPadding(32,8,32,64)

        tableRow.addView(editText)
        tableRow.addView(btnDateCk)

        tableLayout.addView(tableRow)


        newList.add(tableRow)

        totalCheckpoints+=1
    }


    //TODO: Create dialog boxes for remove item and picking date and time
  /*  fun removeItemDialog(tr: TableRow){
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Delete Checkpoint")
        dialog.setMessage("Do you want to delete this checkpoint")
        dialog.setPositiveButton( "Yes", DialogInterface.OnClickListener { dialogInterface, i ->
            Log.d("TAG", "Deleting a checkpoint row")
            tableLayout.removeView(tr)
            cpList.remove(tr)
            Toast.makeText(this,"Deleted!",Toast.LENGTH_SHORT).show()

        })
        dialog.setNegativeButton("No", DialogInterface.OnClickListener { dialogInterface, i ->
            Toast.makeText(this,"Canceled!",Toast.LENGTH_SHORT).show()
        })
        //?dialog.create()
        dialog.show()

    }
*/
    fun timePickerDialog(btn:Button){
        val view = View.inflate(this,R.layout.dialog_timepicker, null)
        val tp = view.findViewById<TimePicker>(R.id.timePicker)

        val dialog = AlertDialog.Builder(this)
        dialog.setView(view)
        dialog.setTitle("TimePicker!")
        dialog.setPositiveButton("Confirm"
        ) { _: DialogInterface, _:Int ->
            btn.text = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                "${tp.hour}:${tp.minute}"
            else
                "${tp.currentHour}:${tp.currentMinute}"

        }
        dialog.create()
        dialog.show()

    }
    fun datePickerDialog(btn:Button){
        val view = View.inflate(this,R.layout.dialog_datepicker, null)
        val dp = view.findViewById<DatePicker>(R.id.datePicker)

        val dialog = AlertDialog.Builder(this)
        var tmp = ""
        dialog.setView(view)
        dialog.setTitle("TimePicker!")
        dialog.setPositiveButton("Confirm"
        ) { _: DialogInterface, _:Int ->
            btn.text =  "${dp.year}${dp.month}${dp.dayOfMonth}"

        }
        if(btn.id == R.id.btnPickDueDate){
            date = tmp
        }
        dialog.create()
        dialog.show()

    }


    private fun submit() {
        val db = DatabaseHelper(this)

        for (i:Int in 0 until newList.size){
            val et =  newList.get(i).getChildAt(0) as EditText
            val d = (newList.get(i).getChildAt(1) as Button).text.toString()
          //  val t = (newList.get(i).getChildAt(2) as Button).text.toString()
            val ck = Checkpoint(et.text.toString(),d,"time",false, goalId)

            db.addCheckpointData(ck)
            Log.d("TAG", "${et.text}")
        }

        mapOld.forEach{
            val id = it.key
            val updatedName = (it.value.getChildAt(0) as EditText).text.toString()
            val updatedDate = (it.value.getChildAt(1) as Button).text.toString()

            db.updateCheckpointData(id,updatedName,updatedDate)
        }

        val df = DecimalFormat("#.##")
        val newPercent = (df.format(((totalCheckpointsCompleted/totalCheckpoints) * 100))).toString()
        Log.d("TAG!", "nP " + newPercent + " " + totalCheckpoints + " " + totalCheckpointsCompleted)
        db.updateGoalNameAndDate(goalId,etGoalName.text.toString(), date, newPercent)


        val i = Intent(this, SideNavDrawer::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(i)
    }


}
