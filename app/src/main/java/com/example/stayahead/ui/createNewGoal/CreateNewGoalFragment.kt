package com.example.stayahead.ui.createNewGoal

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.children
import androidx.core.view.marginLeft
import androidx.core.view.marginStart
import androidx.core.view.marginTop
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.stayahead.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_create_new_goal.*
import java.sql.Time
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random.Default.nextInt

class CreateNewGoalFragment : Fragment() {

    private lateinit var toolsViewModel: CreateNewGoalViewModel
    private lateinit var root: View
    private lateinit var layout: LinearLayout
    private lateinit var tvDateAndTime: TextView
    private lateinit var newGoal: Goal
    private lateinit var cpList: ArrayList<LinearLayout>
    private lateinit var btnSubmit: Button
    private lateinit var sharedPreferences: SharedPreferences
    private var limit:Int = 0
    private var hour:Int = 0
    private var minute:Int = 0
    val goalDateTimeToAlarm = Calendar.getInstance(Locale.getDefault())
    private val TAG = "CreateNewGoalFragment:"
    private var goalDate = "-"
    private var goalTime = "-"
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        toolsViewModel =
            ViewModelProviders.of(this).get(CreateNewGoalViewModel::class.java)
        root = inflater.inflate(R.layout.fragment_create_new_goal, container, false)
        sharedPreferences = root.context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        hour = sharedPreferences.getInt("notification_time_hour",9)
        minute = sharedPreferences.getInt("notification_time_minute",0)
        limit = sharedPreferences.getInt("limit_checkpoints",50)
        Log.d("TAG:", " goaldatetime is " + goalDateTimeToAlarm.timeInMillis)
        val btnDueDate: Button = root.findViewById(R.id.btnPickDueDate)
        val btnDueTime: Button = root.findViewById(R.id.btnPickDueTime)
        btnSubmit = root.findViewById(R.id.btnSubmitNewGoal)
        val btnAddCheckpoint: Button = root.findViewById(R.id.btnAddCheckpoint)
        tvDateAndTime = root.findViewById(R.id.tvDueDate)
        layout = root.findViewById(R.id.lvCheckpoints)
        cpList = ArrayList()
        //layout.setBackgroundColor(Color.LTGRAY)

        btnDueDate.setOnClickListener {
            datePickerDialog(btnDueDate)
        }
        btnDueTime.setOnClickListener {
            timePickerDialog(btnDueTime)
        }
        btnSubmit.setOnClickListener{
            if(etGoalName.text.toString() != "" && goalDate != "-" && goalTime != "-")
                submit()
            else
                Toast.makeText(root.context,"A goal must have a name, date and time",Toast.LENGTH_SHORT).show()
        }
        btnAddCheckpoint.setOnClickListener{
            if(cpList.size <= limit)
                createCheckpoint()
            else{
                Toast.makeText(root.context, "Limited to only $limit checkpoints", Toast.LENGTH_SHORT).show()
                Log.d("TAG:", "limit is "+ limit)
                Log.d("TAG:", " size is "+ cpList.size)
            }
        }

        return root
    }

    private fun submit() {
        val db = DatabaseHelper(root.context)
        val id = (db.getGoalDBCount() + 1)
        Log.d(TAG,"id is "+ id)
        // can get a view by its id name


        newGoal = Goal(etGoalName.text.toString(),"0.0", goalDate, goalTime,
            false, id)
        for (i:Int in 0 until cpList.size){
            val cpName =  (cpList.get(i).getChildAt(0) as EditText).text.toString()
            var cpDate = ((cpList.get(i).getChildAt(1) as LinearLayout).getChildAt(0) as Button).text.toString()
            var cpTime = ((cpList.get(i).getChildAt(1) as LinearLayout).getChildAt(1) as Button).text.toString()
            if(cpDate == "Date")
                cpDate = newGoal.date
            if(cpTime == "Time")
                cpTime = newGoal.time
            val ck = Checkpoint(cpName, cpDate, cpTime,false, id,db.getCheckpointDBCount()+1)
            Log.d("TAG:", "et is $cpName d is $cpDate  d2 is $cpTime")

            val  cpTimeInMillis  = getCheckpointTimeInMillis(cpDate, cpTime)
            if(sharedPreferences.getInt("send_checkpoint",1) == 1) {
                Log.d("TAG:","Checkpoint in millis " + cpTimeInMillis + " ::  cp id " + ck.checkpointId)

                val pendingIntent = AlarmReceiver.createPendingIntent(root.context, ck.checkpointId, "checkpoint", newGoal.goalName, newGoal.goalId)

                AlarmReceiver.createAlarmManager(root.context, pendingIntent, cpTimeInMillis)
                //createAlarmManager(ck.checkpointId, "checkpoint", cpTimeInMillis)


            }
            newGoal.addCheckpoint(ck)
            db.addCheckpointData(ck)
            Log.d(TAG, "$cpName")

        }

        if(sharedPreferences.getInt("send_goal",1) == 1) {
            val pendingIntent = AlarmReceiver.createPendingIntent(root.context, newGoal.goalId, "goal", newGoal.goalName, newGoal.goalId)

            AlarmReceiver.createAlarmManager(root.context, pendingIntent, goalDateTimeToAlarm.timeInMillis)
            //createAlarmManager(newGoal.goalId, "goal", goalDateTimeToAlarm.timeInMillis)
        }
        db.addGoalData(newGoal)
        parentFragmentManager.popBackStack()
    }

    private fun getCheckpointTimeInMillis(checkpointDate:String, checkpointTime: String):Long{
        var dateArr = checkpointDate.split("-")
        var timeArr = checkpointTime.split(":")
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

    /*private fun createAlarmManager(resultCode:Int, typeValue: String, time:Long){
        Log.d("TAG:", " CAM: time is " + time + " s "+ typeValue)
        val pendingIntent = createPendingIntent(resultCode,typeValue)
        val am = root.context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        //am.setExact(AlarmManager.RTC_WAKEUP, time, pendingIntent)
        am.setRepeating(AlarmManager.RTC_WAKEUP, time, time,pendingIntent)
    }
    private fun createPendingIntent(resultCode:Int, typeValue:String) : PendingIntent{
        val notifyIntent = Intent(root.context, AlarmReceiver::class.java)
        notifyIntent.putExtra("goal_name",newGoal.goalName)
        notifyIntent.putExtra("type",typeValue)
        notifyIntent.putExtra("code",resultCode)
        notifyIntent.putExtra("goal_id", newGoal.goalId)
        return PendingIntent.getBroadcast(root.context, resultCode,
            notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    }
    */
    private fun removeItemDialog(tr: TableRow){
        val dialog = AlertDialog.Builder(root.context)
        dialog.setTitle("Delete Checkpoint")
        dialog.setMessage("Do you want to delete this checkpoint")
        dialog.setPositiveButton( "Yes", DialogInterface.OnClickListener { dialogInterface, i ->
            Log.d(TAG, "Deleting a checkpoint row")
            layout.removeView(tr)
            cpList.remove(tr)
            Toast.makeText(root.context,"Deleted!",Toast.LENGTH_SHORT).show()

        })
        dialog.setNegativeButton("No", DialogInterface.OnClickListener { dialogInterface, i ->
            Toast.makeText(root.context,"Canceled!",Toast.LENGTH_SHORT).show()
        })
        dialog.show()

    }

    private fun datePickerDialog(btn:Button){
        val view = View.inflate(root.context,R.layout.dialog_datepicker, null)
        val dp = view.findViewById<DatePicker>(R.id.datePicker)
        var tmpDate =""
        val dialog = AlertDialog.Builder(root.context)
        val d = Date()
        dp.minDate = d.time
        //Toast.makeText(root.context,"min is " + dp.minDate + " : " + d, Toast.LENGTH_SHORT).show()
        dialog.setView(view)
        dialog.setTitle("Date Picker")
        dialog.setPositiveButton("Confirm"
        ) { _:DialogInterface, _:Int ->
            tmpDate = if((dp.month + 1)<10) {
                "${dp.year}-0${(dp.month + 1)}-${dp.dayOfMonth}"
            } else {
                "${dp.year}-${(dp.month + 1)}-${dp.dayOfMonth}"
            }

            if(btn.id == R.id.btnPickDueDate){

                goalDateTimeToAlarm.set(Calendar.YEAR, dp.year)
                goalDateTimeToAlarm.set(Calendar.MONTH, dp.month)
                goalDateTimeToAlarm.set(Calendar.DAY_OF_MONTH, dp.dayOfMonth )


                goalDate = tmpDate
                val tmp = "Due on : $goalDate at $goalTime"
                tvDateAndTime.text = tmp

                Log.d("TAG:", " goaldatetime is " + goalDateTimeToAlarm.timeInMillis)

            }
            else {

                val dateTime = Calendar.getInstance(Locale.getDefault())//0//d.time
                dateTime.set(Calendar.YEAR, dp.year)
                dateTime.set(Calendar.MONTH, dp.month)
                dateTime.set(Calendar.DAY_OF_MONTH, dp.dayOfMonth )
                dateTime.set(Calendar.HOUR_OF_DAY, 0)
                dateTime.set(Calendar.MINUTE, 0)
                dateTime.set(Calendar.SECOND, 0)
                if (goalDateTimeToAlarm.timeInMillis < dateTime.timeInMillis) {
                    Toast.makeText(root.context, "Can't have a checkpoint due after goal's date", Toast.LENGTH_LONG).show()
                    btn.text = goalDate
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
        val dialog = AlertDialog.Builder(root.context)
        val view = View.inflate(root.context, R.layout.dialog_timepicker,null)
        val tp = view.findViewById<TimePicker>(R.id.timePicker)
        dialog.setView(view)
        dialog.setTitle("Time Picker")
        var tpHour = hour
        var tpMin = minute
        var tmpTime = ""
        dialog.setPositiveButton("Confirm"){ _,_ ->
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                Toast.makeText(root.context, "TP: " + tp.hour + " : " + tp.minute, Toast.LENGTH_SHORT).show()
                tpHour = tp.hour
                tpMin = tp.minute
            }
            else {
                Toast.makeText(root.context, "TP: " + tp.currentHour + " : " + tp.currentMinute, Toast.LENGTH_SHORT).show()
                tpHour = tp.currentHour
                tpMin = tp.currentMinute
            }

            tmpTime = "$tpHour:$tpMin"


            if(btn.id == R.id.btnPickDueTime){
                goalTime = tmpTime
                val tmp = "Due on : $goalDate at $goalTime"
                tvDateAndTime.text = tmp

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

    private fun createCheckpoint(){
        val linearLayout = layoutInflater.inflate(R.layout.test_list_view_item, null)
        val linearLayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        linearLayoutParams.setMargins(16,16,16,16)
        linearLayout.layoutParams = linearLayoutParams
        linearLayout.background = root.context.getDrawable(R.drawable.dashed_full_border)
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

        cpList.add((linearLayout as LinearLayout))

        layout.addView(linearLayout)
    }

   /* private fun createCheckpoint(){
        val linearLayout = layoutInflater.inflate(R.layout.test_list_view_item, null) as LinearLayout //LinearLayout(root.context)
        val linearLayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT,1f)
        //linearLayout.orientation = LinearLayout.VERTICAL

        val linearLayoutButtons = LinearLayout(root.context)
        val linearLayoutButtonsParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT)
        //linearLayoutButtons.orientation = LinearLayout.HORIZONTAL

        val tableRow = TableRow(root.context)
        val trParams = TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,TableLayout.LayoutParams.WRAP_CONTENT,1f)
        //trParams.setMargins(32,32,32,32)
        val etParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        val editText = EditText(root.context)
       // etParams.setMargins(32,32,32,32)
        val btnDateCk =  Button(root.context)//Button(ContextThemeWrapper(root.context, R.style.Widget_MaterialComponents_Button_OutlinedButton))
        val btnParams = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        val btnTimeCk =  Button(root.context)//Button(ContextThemeWrapper(root.context, R.style.Widget_MaterialComponents_Button_OutlinedButton))
        val btnTimeParams = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);

        // btnParams.setMargins(32,32,32,32)
        //tableRow.setBackgroundColor(Color.WHITE)

        btnDateCk.setOnClickListener {
            datePickerDialog(btnDateCk)
        }

        tableRow.layoutParams = trParams

        btnDateCk.text = "Date"
        btnTimeCk.text = "Time"
        //btnDateCk.setBackgroundResource( root.context.resources R.style.Widget_MaterialComponents_Button_OutlinedButton
        //btnDateCk.setBackgroundColor(Color.parseColor("#f0f0f0"))
        //btnDateCk.setTextColor(root.context.getResources().getColor(R.color.colorPrimaryDark5))
        //btnDateCk.setBackgroundResource(R.drawable.full_border)
        btnDateCk.layoutParams = btnParams
        btnTimeCk.layoutParams = btnTimeParams

        editText.layoutParams = etParams
        editText.hint = "Enter a checkpoint!"
        editText.setPadding(32,8,32,64)

        //linearLayout.layoutParams = linearLayoutParams
        linearLayoutButtons.layoutParams = linearLayoutButtonsParams

        linearLayoutButtons.setBackgroundColor(Color.GREEN)

        layout.addView(linearLayout)
      //  val linearList = listOf<LinearLayout>(linearLayout)

        //val adapter = ArrayAdapter<LinearLayout>(root.context,R.layout.test_list_view_item, linearList)

        //layout.adapter = adapter
        //layout.addView(linearLayout)
        cpList.add(tableRow)

    }*/
}