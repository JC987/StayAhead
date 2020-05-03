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
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.children
import androidx.core.view.marginLeft
import androidx.core.view.marginStart
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
    private lateinit var layout: TableLayout
    private lateinit var tvDateAndTime: TextView
    private lateinit var newGoal: Goal
    private lateinit var cpList: ArrayList<TableRow>
    private lateinit var btnSubmit: Button
    private lateinit var sharedPreferences: SharedPreferences
    private var hour:Int = 0
    private var minute:Int = 0
    val goalDateTimeToAlarm = Calendar.getInstance(Locale.getDefault())
    private val TAG = "CreateNewGoalFragment:"
    private var goalDate = ""
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
        val btnDueDate: Button = root.findViewById(R.id.btnPickDueDate)
        btnSubmit = root.findViewById(R.id.btnSubmitNewGoal)
        val btnAddCheckpoint: Button = root.findViewById(R.id.btnAddCheckpoint)
        tvDateAndTime = root.findViewById(R.id.tvDueDate)
        layout = root.findViewById(R.id.lvCheckpoints)
        cpList = ArrayList()
        //layout.setBackgroundColor(Color.LTGRAY)

        btnDueDate.setOnClickListener {
            datePickerDialog(btnDueDate)
        }
        btnSubmit.setOnClickListener{
            submit()
        }
        btnAddCheckpoint.setOnClickListener{
            createCheckpoint()
        }

        return root
    }

    private fun submit() {
        val db = DatabaseHelper(root.context)
        val id = (db.getGoalDBCount() + 1)
        Log.d(TAG,"id is "+ id)
        // can get a view by its id name


        newGoal = Goal(etGoalName.text.toString(),"0.0", goalDate,
            false, id)
        for (i:Int in 0 until cpList.size){
            val et =  cpList.get(i).getChildAt(0) as EditText
            val d = (cpList.get(i).getChildAt(1) as Button).text.toString()
            val ck = Checkpoint(et.text.toString(),d,"time",false, id,db.getCheckpointDBCount()+1)

            val cpTime = getCheckpointTimeInMillis(d)
            createAlarmManager(ck.checkpointId,"checkpoint", cpTime)

            newGoal.addCheckpoint(ck)
            db.addCheckpointData(ck)
            Log.d(TAG, "${et.text}")
        }


        createAlarmManager(newGoal.goalId,"goal",goalDateTimeToAlarm.timeInMillis)

        db.addGoalData(newGoal)
        parentFragmentManager.popBackStack()
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
        val pendingIntent = createPendingIntent(resultCode,typeValue)
        val am = root.context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.setExact(AlarmManager.RTC_WAKEUP,time, pendingIntent)

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
        Toast.makeText(root.context,"min is " + dp.minDate + " : " + d, Toast.LENGTH_SHORT).show()
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
                goalDateTimeToAlarm.set(Calendar.HOUR_OF_DAY, hour)
                goalDateTimeToAlarm.set(Calendar.MINUTE, minute)
                goalDateTimeToAlarm.set(Calendar.SECOND, 0)

                goalDate = tmpDate
                val tmp = "Due Date is: " + goalDate
                tvDateAndTime.text = tmp
            }
            else {

                if(goalDate < tmpDate){
                    Toast.makeText(root.context,"Can't have a checkpoint due after goal's date", Toast.LENGTH_LONG).show()
                    btn.text = goalDate
                }
                else
                    btn.text = tmpDate
            }

        }


        dialog.create()
        dialog.show()

    }

    private fun createCheckpoint(){
        val tableRow = TableRow(root.context)
        val trParams = TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,TableLayout.LayoutParams.WRAP_CONTENT)
        //trParams.setMargins(32,32,32,32)
        val etParams = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 10f);
        val editText = EditText(root.context)
       // etParams.setMargins(32,32,32,32)
        val btnDateCk =  Button(root.context)//Button(ContextThemeWrapper(root.context, R.style.Widget_MaterialComponents_Button_OutlinedButton))
        val btnParams = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1f);
       // btnParams.setMargins(32,32,32,32)
        //tableRow.setBackgroundColor(Color.WHITE)

        btnDateCk.setOnClickListener {
            datePickerDialog(btnDateCk)
        }

        tableRow.layoutParams = trParams

        btnDateCk.text = "Date"
        //btnDateCk.setBackgroundResource( root.context.resources R.style.Widget_MaterialComponents_Button_OutlinedButton
        //btnDateCk.setBackgroundColor(Color.parseColor("#f0f0f0"))
        //btnDateCk.setTextColor(root.context.getResources().getColor(R.color.colorPrimaryDark5))
        //btnDateCk.setBackgroundResource(R.drawable.full_border)
        btnDateCk.layoutParams = btnParams

        editText.layoutParams = etParams
        editText.hint = "Enter a checkpoint!"
        editText.setPadding(32,8,32,64)

        tableRow.addView(editText)
        tableRow.addView(btnDateCk)
        layout.addView(tableRow)

        editText.setOnLongClickListener {
            removeItemDialog(tableRow)
            true
        }
        cpList.add(tableRow)

    }
}