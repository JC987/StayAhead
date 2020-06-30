package com.example.stayahead.ui.settings

import android.app.AlarmManager
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.stayahead.*

class SettingsFragment : Fragment() {

    private lateinit var root: View
    //private lateinit var btnTimePicker:Button
    private lateinit var cbSendGoalNotifications:CheckBox
    private lateinit var cbSendCheckpointNotifications:CheckBox
    private lateinit var cbAutoComplete:CheckBox
    private lateinit var spCheckpointLimit:Spinner
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_settings, container, false)
        val btnTruncate: Button = root.findViewById(R.id.btnDropTables)
        //btnTimePicker = root.findViewById(R.id.btnSettingsTimePicker)
        sharedPreferences = root.context.getSharedPreferences("settings", Context.MODE_PRIVATE)

        cbSendGoalNotifications = root.findViewById(R.id.cbSettingsGoalNotification)
        cbSendCheckpointNotifications = root.findViewById(R.id.cbSettingsCheckpointNotification)
        cbAutoComplete = root.findViewById(R.id.cbAutoCompleteGoal)
        spCheckpointLimit = root.findViewById(R.id.spCheckpointLimit)

        loadSettings()

        btnTruncate.setOnClickListener {
            createTruncateDialog()
        }
       /* btnTimePicker.setOnClickListener {
            createTimePickerDialog()
        }*/

        cbSendCheckpointNotifications.setOnClickListener {
            val editor = sharedPreferences.edit()
            if(cbAutoComplete.isChecked) {

                editor.putInt("send_checkpoint",1)
            }
            else{
                editor.putInt("send_checkpoint",0)
            }
            editor.apply()
        }
        cbSendGoalNotifications.setOnClickListener {
            val editor = sharedPreferences.edit()
            if(cbAutoComplete.isChecked) {

                editor.putInt("send_goal",1)
            }
            else{
                editor.putInt("send_goal",0)
            }
            editor.apply()
        }
        cbAutoComplete.setOnClickListener {
            val editor = sharedPreferences.edit()
            if(cbAutoComplete.isChecked) {

                editor.putInt("auto_complete",1)
            }
            else{
                editor.putInt("auto_complete",0)
            }
            editor.apply()
        }

        spCheckpointLimit.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val editor = sharedPreferences.edit()
                val value = Integer.parseInt(spCheckpointLimit.getItemAtPosition(position).toString())
                editor.putInt("limit_checkpoints",value)
                editor.putInt("limit_checkpoints_pos",position)
                editor.apply()
                //no need for when statement
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Code to perform some action when nothing is selected
            }
        }

        return root
    }

    private fun loadSettings() {
        //btnTimePicker.text = "${sharedPreferences.getInt("notification_time_hour",9)}:${sharedPreferences.getInt("notification_time_minute",0)}"
        cbSendGoalNotifications.isChecked = (sharedPreferences.getInt("send_goal",1) == 1)
        cbSendCheckpointNotifications.isChecked = (sharedPreferences.getInt("send_checkpoint", 1) == 1)
        cbAutoComplete.isChecked = (sharedPreferences.getInt("auto_complete", 0) == 1)
        spCheckpointLimit.setSelection(sharedPreferences.getInt("limit_checkpoints_pos",0))
    }

    private fun createTruncateDialog() {
        val dialog = AlertDialog.Builder(root.context)
        dialog.setTitle("Clear all data!")
        dialog.setMessage("Clear all saved data! Can not be undone! Are you sure you want to continue?")
        dialog.setPositiveButton("Yes") { dialogInterface, i ->
            Toast.makeText(root.context,"Deleting data",Toast.LENGTH_SHORT).show()
            cancelAlarmManagers()
            clearAllNotification()
            val db = DatabaseHelper(root.context)
            db.truncateTables()

            db.close()
        }
        dialog.setNegativeButton("No"){ dialogInterface, i ->
            //Toast.makeText(root.context, "NOO",Toast.LENGTH_SHORT).show();
        }
        dialog.create()
        dialog.show()
    }

    private fun clearAllNotification(){
        val nh = NotificationHelper(root.context)
        nh.getManager().cancelAll()
    }

    private fun cancelAlarmManagers(){
        val db = DatabaseHelper(root.context)
        val cursor = db.getActiveGoalsData(false)
        while(cursor.moveToNext()){
            val currentGoal = Goal(cursor.getString(1),cursor.getString(2),cursor.getString(3),cursor.getString(4),false,cursor.getInt(0))
            val pendingIntentGoal = AlarmReceiver.createPendingIntent(root.context, currentGoal.goalId, "goal", currentGoal.goalName, currentGoal.goalId)
            val alarmManagerGoal = root.context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManagerGoal.cancel(pendingIntentGoal)

            val checkpointCursor = db.getAllCheckpointsOfGoal(currentGoal.goalId)
            while(checkpointCursor.moveToNext()){
                val checkpoint = Checkpoint(checkpointCursor.getString(1),checkpointCursor.getString(3),checkpointCursor.getString(4), false, checkpointCursor.getInt(2),checkpointCursor.getInt(0))
                val pendingIntentCheckpoint = AlarmReceiver.createPendingIntent(root.context, checkpoint.checkpointId + 100000, "checkpoint", currentGoal.goalName, currentGoal.goalId)
                val alarmManagerCheckpoint = root.context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                alarmManagerCheckpoint.cancel(pendingIntentCheckpoint)
            }
        }
        db.close()
    }

    private fun createTimePickerDialog(){
        val view = View.inflate(root.context, R.layout.dialog_timepicker, null)
        val tp = view.findViewById<TimePicker>(R.id.timePicker)

        val dialog = AlertDialog.Builder(root.context)
        dialog.setTitle("Set notification time")
        dialog.setView(view)
        dialog.setPositiveButton("Confirm") { _: DialogInterface, _: Int ->
            if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                //btnTimePicker.text = "${tp.currentHour}:${tp.currentMinute}"
                val editor = sharedPreferences.edit()
                editor.putInt("notification_time_hour",tp.currentHour)
                editor.putInt("notification_time_minute",tp.currentMinute)
                editor.apply()
            }
            else {
                //btnTimePicker.text = "${tp.hour}:${tp.minute}"
                val editor = sharedPreferences.edit()
                editor.putInt("notification_time_hour",tp.hour)
                editor.putInt("notification_time_minute",tp.minute)
                editor.apply()
            }

            Toast.makeText(root.context,"time updated",Toast.LENGTH_SHORT).show()
        }

        dialog.create()
        dialog.show()
    }
}












