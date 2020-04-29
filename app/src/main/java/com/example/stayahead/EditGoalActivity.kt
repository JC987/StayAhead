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
import androidx.core.view.get
import kotlinx.android.synthetic.main.activity_edit_goal.*
import kotlinx.android.synthetic.main.fragment_create_new_goal.*
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList

class EditGoalActivity : AppCompatActivity() {
    var goalId = -1
    lateinit var  tableLayout :TableLayout
    private lateinit var currentGoal: Goal
    private  var oldList: ArrayList<Checkpoint> = ArrayList()
    val mapOld = mutableMapOf<Int, TableRow>()
    private  var newList: ArrayList<TableRow> = ArrayList()
    private lateinit var etGoalName: EditText
    private lateinit var tvGoalDate: TextView
    private var hour:Int = 0
    private var minute:Int = 0
    private lateinit var  sharedPreferences:SharedPreferences
    val goalDateTimeToAlarm = Calendar.getInstance(Locale.getDefault())

    var goalDate = ""
    var totalCheckpoints:Float = 0f
    var totalCheckpointsCompleted:Float = 0f
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_create_new_goal)
        goalId = intent.getIntExtra("goal_id",1)
        val db = DatabaseHelper(this)
        sharedPreferences = getSharedPreferences("settings",Context.MODE_PRIVATE)
        etGoalName = findViewById<EditText>(R.id.etGoalName)
        tvGoalDate = findViewById<TextView>(R.id.tvDueDate)
        hour = sharedPreferences.getInt("notification_time_hour",9)
        minute = sharedPreferences.getInt("notification_time_minute",0)
        val btnDate = findViewById<Button>(R.id.btnPickDueDate)
        val btnAddCheckpoint = findViewById<Button>(R.id.btnAddCheckpoint)
         tableLayout = findViewById<TableLayout>(R.id.lvCheckpoints)
        val btnSubmit = findViewById<Button>(R.id.btnSubmitNewGoal)


        val c = db.getGoal(goalId)
        c.moveToNext()
        Log.d("TAG","count is " + c.count)
        Log.d("TAG",c.getString(1))
        etGoalName.setText(c.getString(1))
//        currentGoal.goalName = c.getString(1)
        goalDate = c.getString(3)
  //      currentGoal.date = c.getString(3)
        tvGoalDate.text = "Due date is: " + goalDate
        currentGoal = Goal(c.getString(1),"",c.getString(3),false,goalId)

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

    fun datePickerDialog(btn:Button){
        val view = View.inflate(this,R.layout.dialog_datepicker, null)
        val dp = view.findViewById<DatePicker>(R.id.datePicker)

        val dialog = AlertDialog.Builder(this)

        dialog.setView(view)
        dialog.setTitle("Date Picker")
        dialog.setPositiveButton("Confirm"
        ) { _: DialogInterface, _:Int ->
           var tmpDate =  if((dp.month + 1)<10)
                "${dp.year}-0${(dp.month + 1)}-${dp.dayOfMonth}"
            else
                 "${dp.year}-${(dp.month + 1)}-${dp.dayOfMonth}"

            if(btn.id == R.id.btnPickDueDate){
                goalDate = tmpDate
                val tmp = "Due Date is: " + goalDate
                tvGoalDate.text = tmp

                goalDateTimeToAlarm.set(Calendar.YEAR, dp.year)
                goalDateTimeToAlarm.set(Calendar.MONTH, dp.month)
                goalDateTimeToAlarm.set(Calendar.DAY_OF_MONTH, dp.dayOfMonth )
                goalDateTimeToAlarm.set(Calendar.HOUR_OF_DAY, hour)
                goalDateTimeToAlarm.set(Calendar.MINUTE, minute)
                goalDateTimeToAlarm.set(Calendar.SECOND, 0)


            }
            else
                if(goalDate < tmpDate){
                    Toast.makeText(this,"Can't have a checkpoint due after goal's date", Toast.LENGTH_LONG).show()
                    btn.text = goalDate
                }
                else
                    btn.text = tmpDate
        }
        dialog.create()
        dialog.show()

    }


    private fun submit() {
        val db = DatabaseHelper(this)

        for (i:Int in 0 until newList.size){
            val et =  newList.get(i).getChildAt(0) as EditText
            val d = (newList.get(i).getChildAt(1) as Button).text.toString()
            val ck = Checkpoint(et.text.toString(),d,"time",false, goalId,db.getCheckpointDBCount()+1)

            val cpTime = getCheckpointTimeInMillis(d)
            createAlarmManager(ck.checkpointId,"checkpoint", cpTime)


            db.addCheckpointData(ck)
            Log.d("TAG", "${et.text}")
        }

        mapOld.forEach{
            val id = it.key
            val updatedName = (it.value.getChildAt(0) as EditText).text.toString()
            val updatedDate = (it.value.getChildAt(1) as Button).text.toString()
            val ck = Checkpoint(updatedName,updatedDate,"time",false, goalId,id)

            val cpTime = getCheckpointTimeInMillis(updatedDate)
            updateAlarmManager(ck.checkpointId,"checkpoint", cpTime)


            db.updateCheckpointData(id,updatedName,updatedDate)
        }

        val df = DecimalFormat("0.0")
        val newPercent = (df.format(((totalCheckpointsCompleted/totalCheckpoints) * 100))).toString()
        Log.d("TAG!", "nP " + newPercent + " " + totalCheckpoints + " " + totalCheckpointsCompleted)
        db.updateGoalNameAndDate(goalId,etGoalName.text.toString(), goalDate, newPercent)

        updateAlarmManager(goalId,"goal",goalDateTimeToAlarm.timeInMillis)

        Toast.makeText(this,"Goal Edited",Toast.LENGTH_SHORT).show()
        val i = Intent(this, SideNavDrawer::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(i)
    }


    private fun getCheckpointTimeInMillis(checkpointDate:String):Long{
        var sp = checkpointDate.split("-")
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
