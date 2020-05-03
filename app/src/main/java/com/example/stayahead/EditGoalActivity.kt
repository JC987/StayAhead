package com.example.stayahead

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList

class EditGoalActivity : AppCompatActivity() {
    private lateinit var  tableLayout :TableLayout
    private lateinit var etGoalName: EditText
    private lateinit var tvGoalDate: TextView
    private lateinit var currentGoal: Goal
    private val oldCheckpoints = mutableMapOf<Int, TableRow>()
    private var newCheckpoints: ArrayList<TableRow> = ArrayList()
    private var isGoalDateChanged = false
    private var hour:Int = 0
    private var minute:Int = 0
    private lateinit var  sharedPreferences:SharedPreferences

    private val goalDateTimeToAlarm = Calendar.getInstance(Locale.getDefault())

    private var numOfCheckpoints:Float = 0f
    private var numOfCheckpointsCompleted:Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_create_new_goal)

        loadViewData()

        loadGoalData()

        loadCheckpointData()

    }

    private fun loadCheckpointData(){
        val db = DatabaseHelper(this)
        val checkpointCursor = db.getAllCheckpointsOfGoal(currentGoal.goalId)
        while(checkpointCursor.moveToNext()){

            val tableRow = TableRow(this)
            val trParams = TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,TableLayout.LayoutParams.WRAP_CONTENT)

            val etParams = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 10f);
            val editText = EditText(this)

            val btnDateCk = Button(this)
            val btnParams = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1f);

            tableRow.layoutParams = trParams
            editText.layoutParams = etParams
            btnDateCk.layoutParams = btnParams

            editText.setText(checkpointCursor.getString(1))
            btnDateCk.text = checkpointCursor.getString(3)

            btnDateCk.setOnClickListener{
                datePickerDialog(btnDateCk)
            }

            tableRow.addView(editText)
            tableRow.addView(btnDateCk)
            tableLayout.addView(tableRow)

            oldCheckpoints[checkpointCursor.getInt(0)] = tableRow//same as .put

            if(checkpointCursor.getInt(4) == 1){
                numOfCheckpointsCompleted+=1
            }
            numOfCheckpoints+=1
        }

    }

    private fun loadGoalData(){
        val db = DatabaseHelper(this)
        val goalCursor = db.getGoal(intent.getIntExtra("goal_id",1))
        goalCursor.moveToNext()
        Log.d("TAG","count is " + goalCursor.count)
        Log.d("TAG",goalCursor.getString(1))
        Log.d("TAG",goalCursor.getString(3))
        etGoalName.setText(goalCursor.getString(1))

        currentGoal = Goal(goalCursor.getString(1),"",goalCursor.getString(3),false, intent.getIntExtra("goal_id",1))
        tvGoalDate.text = "Due date is: " + currentGoal.date
    }

    private fun loadViewData(){
        //goalId = intent.getIntExtra("goal_id",1)
        tableLayout = findViewById<TableLayout>(R.id.lvCheckpoints)
        sharedPreferences = getSharedPreferences("settings",Context.MODE_PRIVATE)
        etGoalName = findViewById<EditText>(R.id.etGoalName)
        tvGoalDate = findViewById<TextView>(R.id.tvDueDate)
        hour = sharedPreferences.getInt("notification_time_hour",9)
        minute = sharedPreferences.getInt("notification_time_minute",0)
        val btnDate = findViewById<Button>(R.id.btnPickDueDate)
        val btnAddCheckpoint = findViewById<Button>(R.id.btnAddCheckpoint)
        val btnSubmit = findViewById<Button>(R.id.btnSubmitNewGoal)

        btnDate.setOnClickListener {
            datePickerDialog(btnDate)
        }
        btnSubmit.setOnClickListener{
            submit()
        }
        btnAddCheckpoint.setOnClickListener{
            createNewCheckpoint()
        }
    }

    private fun createNewCheckpoint(){
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


        newCheckpoints.add(tableRow)

        numOfCheckpoints+=1
    }



    private fun datePickerDialog(btn:Button){
        val view = View.inflate(this,R.layout.dialog_datepicker, null)
        val dp = view.findViewById<DatePicker>(R.id.datePicker)

        val dialog = AlertDialog.Builder(this)

        dialog.setView(view)
        dialog.setTitle("Date Picker")
        dialog.setPositiveButton("Confirm"
        ) { _: DialogInterface, _:Int ->
           val tmpDate =  if((dp.month + 1)<10)
                "${dp.year}-0${(dp.month + 1)}-${dp.dayOfMonth}"
            else
                 "${dp.year}-${(dp.month + 1)}-${dp.dayOfMonth}"

            if(btn.id == R.id.btnPickDueDate){
                currentGoal.date = tmpDate
                val tmp = "Due Date is: " + currentGoal.date
                tvGoalDate.text = tmp
                isGoalDateChanged = true
                //set the date/time for alarm
                goalDateTimeToAlarm.set(Calendar.YEAR, dp.year)
                goalDateTimeToAlarm.set(Calendar.MONTH, dp.month)
                goalDateTimeToAlarm.set(Calendar.DAY_OF_MONTH, dp.dayOfMonth )
                goalDateTimeToAlarm.set(Calendar.HOUR_OF_DAY, hour)
                goalDateTimeToAlarm.set(Calendar.MINUTE, minute)
                goalDateTimeToAlarm.set(Calendar.SECOND, 0)


            }
            else {
                if (currentGoal.date < tmpDate) {
                    Toast.makeText(this,"Can't have a checkpoint due after goal's date", Toast.LENGTH_LONG).show()
                    btn.text = currentGoal.date
                } else
                    btn.text = tmpDate
            }
        }
        dialog.create()
        dialog.show()

    }

    private fun saveNewCheckpoints(){
        val db = DatabaseHelper(this)

        for (i:Int in 0 until newCheckpoints.size){
            val et =  newCheckpoints.get(i).getChildAt(0) as EditText
            val d = (newCheckpoints.get(i).getChildAt(1) as Button).text.toString()
            val ck = Checkpoint(et.text.toString(),d,"time",false, currentGoal.goalId,db.getCheckpointDBCount()+1)

            val cpTime = getCheckpointTimeInMillis(d)
            createAlarmManager(ck.checkpointId,"checkpoint", cpTime)
            db.addCheckpointData(ck)

            Log.d("TAG", "${et.text}")
        }
    }

    private fun savePreviousCheckpoints(){
        val db = DatabaseHelper(this)

        oldCheckpoints.forEach{
            val id = it.key
            val updatedName = (it.value.getChildAt(0) as EditText).text.toString()
            val updatedDate = (it.value.getChildAt(1) as Button).text.toString()
            val ck = Checkpoint(updatedName,updatedDate,"time",false, currentGoal.goalId, id)

            val cpTime = getCheckpointTimeInMillis(updatedDate)
            updateAlarmManager(ck.checkpointId,"checkpoint", cpTime)
            db.updateCheckpointData(id,updatedName,updatedDate)
        }
    }

    private fun saveGoal(){
        val db = DatabaseHelper(this)

        //calculate new goal percent
        val df = DecimalFormat("0.0")
        val newPercent = (df.format(((numOfCheckpointsCompleted/numOfCheckpoints) * 100))).toString()
        Log.d("TAG", "goaldate is " + currentGoal.date)
        //update checkpoint and alarm manager
        db.updateGoalNameAndDate(currentGoal.goalId,etGoalName.text.toString(), currentGoal.date, newPercent)
        if(isGoalDateChanged)
            updateAlarmManager(currentGoal.goalId,"goal",goalDateTimeToAlarm.timeInMillis)

    }

    private fun submit() {
        saveNewCheckpoints()

        savePreviousCheckpoints()

        saveGoal()

        //create intent
        Toast.makeText(this,"Goal Edited",Toast.LENGTH_SHORT).show()
        val i = Intent(this, SideNavDrawer::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(i)
    }


    private fun getCheckpointTimeInMillis(checkpointDate:String):Long{
        val sp = checkpointDate.split("-")
        val cpDateTimeToAlarm = Calendar.getInstance(Locale.getDefault())
        cpDateTimeToAlarm.set(Calendar.YEAR, sp[0].toInt())
        cpDateTimeToAlarm.set(Calendar.MONTH, (sp[1].toInt() -1) )
        cpDateTimeToAlarm.set(Calendar.DAY_OF_MONTH, sp[2].toInt())
        cpDateTimeToAlarm.set(Calendar.HOUR_OF_DAY, hour)
        cpDateTimeToAlarm.set(Calendar.MINUTE, minute)
        cpDateTimeToAlarm.set(Calendar.SECOND, 0)
        return cpDateTimeToAlarm.timeInMillis
    }
    private fun createAlarmManager(resultCode:Int, typeValue: String, time:Long){
        val pendingIntent = createPendingIntent(resultCode,typeValue,false)
        val am = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.setExact(AlarmManager.RTC_WAKEUP,time, pendingIntent)
    }
    private fun updateAlarmManager(resultCode:Int, typeValue: String, time:Long){
        val pendingIntent = createPendingIntent(resultCode,typeValue,true)
        val am = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.setExact(AlarmManager.RTC_WAKEUP,time, pendingIntent)
    }
    private fun createPendingIntent(resultCode:Int, typeValue:String, update:Boolean) : PendingIntent {
        val notifyIntent = Intent(this, AlarmReceiver::class.java)
        notifyIntent.putExtra("goal_name",currentGoal.goalName)
        notifyIntent.putExtra("type",typeValue)
        notifyIntent.putExtra("code",resultCode)
        notifyIntent.putExtra("goal_id", currentGoal.goalId)
        return if(!update)
            PendingIntent.getBroadcast(this, resultCode, notifyIntent, PendingIntent.FLAG_ONE_SHOT)
        else
            PendingIntent.getBroadcast(this, resultCode, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

}
