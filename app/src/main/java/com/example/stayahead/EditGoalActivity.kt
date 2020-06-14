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
import kotlin.math.min

class EditGoalActivity : AppCompatActivity() {
    private lateinit var  layout :LinearLayout
    private lateinit var etGoalName: EditText
    private lateinit var tvGoalDateAndTime: TextView
    private lateinit var currentGoal: Goal
    private val oldCheckpoints = mutableMapOf<Int, LinearLayout>()
    private var newCheckpoints: ArrayList<LinearLayout> = ArrayList()
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
        Log.d("TAG:","currentGoal Date " + currentGoal.date)
        val x = goalDateTimeToAlarm.timeInMillis
        val goalDateSplit = currentGoal.date.split("-")
        goalDateTimeToAlarm.set(Calendar.YEAR,Integer.parseInt(goalDateSplit[0]))
        goalDateTimeToAlarm.set(Calendar.MONTH, goalDateSplit[1].toInt() - 1)
        goalDateTimeToAlarm.set(Calendar.DAY_OF_MONTH,Integer.parseInt(goalDateSplit[2]))
        goalDateTimeToAlarm.set(Calendar.HOUR, 0 )
        goalDateTimeToAlarm.set(Calendar.MINUTE, 0)
        goalDateTimeToAlarm.set(Calendar.SECOND, 0)

        Log.d("TAG:", "x is "+ x + " h "+ hour + " m " +minute + " month " + (Integer.parseInt(goalDateSplit[1]) - 1))
        Log.d("TAG:", "time is "+ goalDateTimeToAlarm.timeInMillis);
       // Toast.makeText(this," " + x +" : x <- gdtta is " + goalDateTimeToAlarm.timeInMillis,Toast.LENGTH_LONG).show();

    }

    private fun loadCheckpointData(){
        val db = DatabaseHelper(this)
        val checkpointCursor = db.getAllCheckpointsOfGoal(currentGoal.goalId)
        while(checkpointCursor.moveToNext()){
            val linearLayout = layoutInflater.inflate(R.layout.test_list_view_item, null)
            val linearLayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            linearLayoutParams.setMargins(16,16,16,16)
            linearLayout.layoutParams = linearLayoutParams
            linearLayout.background = this.getDrawable(R.drawable.dashed_full_border)
            linearLayout.setPadding(8,16,8,16)

            val editText = linearLayout.findViewById<EditText>(R.id.etEditCheckpointName)
            val btnDateCk = linearLayout.findViewById<Button>(R.id.btnCheckpointDate)
            val btnTimeCk = linearLayout.findViewById<Button>(R.id.btnCheckpointTime)

            btnDateCk.setOnClickListener {
                datePickerDialog(btnDateCk)
            }

            btnTimeCk.setOnClickListener {
                timePickerDialog(btnTimeCk)
            }


            editText.setText(checkpointCursor.getString(1))
            btnDateCk.text = checkpointCursor.getString(3)
            btnTimeCk.text = checkpointCursor.getString(4)

            btnDateCk.setOnClickListener{
                datePickerDialog(btnDateCk)
            }


            layout.addView(linearLayout)

            oldCheckpoints[checkpointCursor.getInt(0)] = (linearLayout as LinearLayout)//same as .put

            if(checkpointCursor.getInt(5) == 1){
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
        var tmp = goalCursor.getString(4).split(":")
        hour = tmp[0].toInt()
        minute = tmp[1].toInt()

        currentGoal = Goal(goalCursor.getString(1),"",goalCursor.getString(3), goalCursor.getString(4),false, intent.getIntExtra("goal_id",1))
        tvGoalDateAndTime.text = "Due on : ${currentGoal.date} at ${currentGoal.time}"
    }

    private fun loadViewData(){
        //goalId = intent.getIntExtra("goal_id",1)
        layout = findViewById<LinearLayout>(R.id.lvCheckpoints)
        sharedPreferences = getSharedPreferences("settings",Context.MODE_PRIVATE)
        etGoalName = findViewById<EditText>(R.id.etGoalName)
        tvGoalDateAndTime = findViewById<TextView>(R.id.tvDueDate)
        hour = sharedPreferences.getInt("notification_time_hour",9)
        minute = sharedPreferences.getInt("notification_time_minute",0)
        val btnDate = findViewById<Button>(R.id.btnPickDueDate)
        val btnTime = findViewById<Button>(R.id.btnPickDueTime)
        val btnAddCheckpoint = findViewById<Button>(R.id.btnAddCheckpoint)
        val btnSubmit = findViewById<Button>(R.id.btnSubmitNewGoal)

        btnDate.setOnClickListener {
            datePickerDialog(btnDate)
        }
        btnTime.setOnClickListener {
            timePickerDialog(btnTime)
        }
        btnSubmit.setOnClickListener{
            submit()
        }
        btnAddCheckpoint.setOnClickListener{
            createNewCheckpoint()
        }
    }

    private fun createNewCheckpoint(){
        val linearLayout = layoutInflater.inflate(R.layout.test_list_view_item, null)
        val linearLayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        linearLayoutParams.setMargins(16,16,16,16)
        linearLayout.layoutParams = linearLayoutParams
        linearLayout.background = this.getDrawable(R.drawable.dashed_full_border)
        linearLayout.setPadding(8,16,8,16)

        val editText = linearLayout.findViewById<EditText>(R.id.etEditCheckpointName)
        val btnDate = linearLayout.findViewById<Button>(R.id.btnCheckpointDate)
        val btnTime = linearLayout.findViewById<Button>(R.id.btnCheckpointTime)

        btnDate.setOnClickListener {
            datePickerDialog(btnDate)
        }

        btnTime.setOnClickListener {
            timePickerDialog(btnTime)
        }

        numOfCheckpoints++
        newCheckpoints.add((linearLayout as LinearLayout))

        layout.addView(linearLayout)
    }

    private fun datePickerDialog(btn:Button){
        val view = View.inflate(this,R.layout.dialog_datepicker, null)
        val dp = view.findViewById<DatePicker>(R.id.datePicker)

        val dialog = AlertDialog.Builder(this)
        val d = Date()
        dp.minDate = d.time

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
                tvGoalDateAndTime.text = tmp
                isGoalDateChanged = true
                //set the date/time for alarm
                goalDateTimeToAlarm.set(Calendar.YEAR, dp.year)
                goalDateTimeToAlarm.set(Calendar.MONTH, dp.month)
                goalDateTimeToAlarm.set(Calendar.DAY_OF_MONTH, dp.dayOfMonth )

                Log.d("TAG:","x again " + goalDateTimeToAlarm.timeInMillis + " " + dp.month + "  " + dp.dayOfMonth)
            }
            else {
                val dateTime = Calendar.getInstance(Locale.getDefault())//0//d.time
                dateTime.set(Calendar.YEAR, dp.year)
                dateTime.set(Calendar.MONTH, dp.month)
                dateTime.set(Calendar.DAY_OF_MONTH, dp.dayOfMonth )
                dateTime.set(Calendar.HOUR_OF_DAY, hour)
                dateTime.set(Calendar.MINUTE, minute)
                dateTime.set(Calendar.SECOND, 0)
                if (currentGoal.date < tmpDate) {
                    Toast.makeText(this, "Can't have a checkpoint due after goal's date", Toast.LENGTH_LONG).show()
                    btn.text = currentGoal.date
                }
                else {
                    btn.text = tmpDate
                }

            }
        }
        dialog.create()
        dialog.show()

    }


    private fun timePickerDialog(btn: Button){
        val dialog = AlertDialog.Builder(this)
        val view = View.inflate(this, R.layout.dialog_timepicker,null)
        val tp = view.findViewById<TimePicker>(R.id.timePicker)
        dialog.setView(view)
        dialog.setTitle("Time Picker")
        var tpHour = hour
        var tpMin = minute
        var tmpTime = ""
        dialog.setPositiveButton("Confirm"){ _,_ ->
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                Toast.makeText(this, "TP: " + tp.hour + " : " + tp.minute, Toast.LENGTH_SHORT).show()
                tpHour = tp.hour
                tpMin = tp.minute
            }
            else {
                Toast.makeText(this, "TP: " + tp.currentHour + " : " + tp.currentMinute, Toast.LENGTH_SHORT).show()
                tpHour = tp.currentHour
                tpMin = tp.currentMinute
            }

            tmpTime = "$tpHour:$tpMin"


            if(btn.id == R.id.btnPickDueTime){
                currentGoal.time = tmpTime
                val tmp = "Due on : ${currentGoal.date} at ${currentGoal.time}"
                tvGoalDateAndTime.text = tmp
                isGoalDateChanged = true
                goalDateTimeToAlarm.set(Calendar.HOUR_OF_DAY, tpHour)
                goalDateTimeToAlarm.set(Calendar.MINUTE, tpMin)
                goalDateTimeToAlarm.set(Calendar.SECOND, 0)
            }
            else{
                btn.text = tmpTime
            }
        }
        dialog.create()
        dialog.show()


    }


    private fun saveNewCheckpoints(){
        val db = DatabaseHelper(this)
        Log.d("asdfasdf:", "time is "+ goalDateTimeToAlarm.timeInMillis);
        for (i:Int in 0 until newCheckpoints.size){
            val et =  newCheckpoints.get(i).getChildAt(0) as EditText
            var cpDate = ((newCheckpoints.get(i).getChildAt(1) as LinearLayout).getChildAt(0) as Button).text.toString()
            var cpTime = ((newCheckpoints.get(i).getChildAt(1) as LinearLayout).getChildAt(1) as Button).text.toString()
            var cpTimeInMillis = goalDateTimeToAlarm.timeInMillis
            Log.d("asdfasdf:", "cpTime is "+ cpTime);
            if(cpDate != "Date") {
                cpTimeInMillis = getCheckpointTimeInMillis(cpDate,cpTime)
            }
            else{
                cpDate = currentGoal.date
            }
            if(cpTime == "Time")
                cpTime = currentGoal.time
            val ck = Checkpoint(et.text.toString(),cpDate,cpTime,false, currentGoal.goalId,db.getCheckpointDBCount()+1)
            if(sharedPreferences.getInt("send_checkpoint",1) == 1) {
                //createAlarmManager(ck.checkpointId, "checkpoint", cpTimeInMillis)
                val pendingIntent = AlarmReceiver.createPendingIntent(this, ck.checkpointId, "checkpoint", currentGoal.goalName, currentGoal.goalId)
                AlarmReceiver.createAlarmManager(this, pendingIntent,cpTimeInMillis)
            }
            db.addCheckpointData(ck)

            Log.d("TAG", "${et.text}")
        }
    }

    private fun savePreviousCheckpoints(){
        val db = DatabaseHelper(this)

        oldCheckpoints.forEach{
            val id = it.key
            val updatedName = (it.value.getChildAt(0) as EditText).text.toString()
            val updatedDate = ((it.value.getChildAt(1) as LinearLayout).getChildAt(0) as Button).text.toString()
            val updatedTime = ((it.value.getChildAt(1) as LinearLayout).getChildAt(1) as Button).text.toString()

            val ck = Checkpoint(updatedName,updatedDate,updatedTime,false, currentGoal.goalId, id)

            val cpTimeInMillis = getCheckpointTimeInMillis(updatedDate,updatedTime)
            Log.d("TAG:","savePreviousCP : cp time " + cpTimeInMillis)
            if(sharedPreferences.getInt("send_checkpoint",1) == 1) {
                Log.d("TAG:","send goal update alarm manager " + currentGoal.goalId + "  " + goalDateTimeToAlarm.timeInMillis)

                val pendingIntent = AlarmReceiver.createPendingIntent(this,ck.checkpointId,"checkpoint", currentGoal.goalName, currentGoal.goalId)
                AlarmReceiver.createAlarmManager(this, pendingIntent, cpTimeInMillis)
                //updateAlarmManager(ck.checkpointId, "checkpoint", cpTimeInMillis)
            }
            db.updateCheckpointData(ck)
        }
    }

    private fun saveGoal(){
        val db = DatabaseHelper(this)
        //TODO: debug for edge cases
        //calculate new goal percent
        val df = DecimalFormat("0.0")
        val newPercent = (df.format(((numOfCheckpointsCompleted/numOfCheckpoints) * 100))).toString()
        Log.d("TAG", "goaldate is " + currentGoal.date)
        //update checkpoint and alarm manager
        currentGoal.goalName = etGoalName.text.toString()
        currentGoal.remainingPercentage = newPercent
        if((sharedPreferences.getInt("send_goal",1) == 1) ) {
            Log.d("TAG:","send goal update alarm manager " + currentGoal.goalId + "  " + goalDateTimeToAlarm.timeInMillis)

            val pendingIntent = AlarmReceiver.createPendingIntent(this,currentGoal.goalId,"goal", currentGoal.goalName, currentGoal.goalId)
            AlarmReceiver.createAlarmManager(this, pendingIntent, goalDateTimeToAlarm.timeInMillis)
            //updateAlarmManager(currentGoal.goalId, "goal", goalDateTimeToAlarm.timeInMillis)
        }
        db.updateGoalNameAndDate(currentGoal)

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


    private fun getCheckpointTimeInMillis(checkpointDate:String, checkpointTime: String):Long{
        val dateArr = checkpointDate.split("-")
        val timeArr = checkpointTime.split(":")
        Log.d("TAG:", " cp time is  $checkpointTime  [0] is ${timeArr[0]} [1] is ${timeArr[1]}")
        val cpDateTimeToAlarm = Calendar.getInstance(Locale.getDefault())
        cpDateTimeToAlarm.set(Calendar.YEAR, dateArr[0].toInt())
        cpDateTimeToAlarm.set(Calendar.MONTH, (dateArr[1].toInt() -1) )
        cpDateTimeToAlarm.set(Calendar.DAY_OF_MONTH, dateArr[2].toInt())
        cpDateTimeToAlarm.set(Calendar.HOUR_OF_DAY, timeArr[0].toInt())
        cpDateTimeToAlarm.set(Calendar.MINUTE, timeArr[1].toInt())
        cpDateTimeToAlarm.set(Calendar.SECOND, 0)
        return cpDateTimeToAlarm.timeInMillis
    }
/*
    private fun createAlarmManager(resultCode:Int, typeValue: String, time:Long){
        val pendingIntent = createPendingIntent(resultCode,typeValue,false)
        val am = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.setExact(AlarmManager.RTC_WAKEUP,time, pendingIntent)
    }
    private fun updateAlarmManager(resultCode:Int, typeValue: String, time:Long){
        Log.d("TAG:","update alarm manager")
        val pendingIntent = createPendingIntent(resultCode,typeValue,true)
        val am = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.setExact(AlarmManager.RTC_WAKEUP,time, pendingIntent)
    }
    private fun createPendingIntent(resultCode:Int, typeValue:String, update:Boolean) : PendingIntent {
        Log.d("TAG:","create pending intent")
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
*/
}
