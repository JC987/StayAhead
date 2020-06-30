package com.example.stayahead

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import java.util.*

class AlarmReceiver : BroadcastReceiver() {
    val TAG:String = "AlarmReceiver"
    override fun onReceive(p0: Context?, p1: Intent?) {

        Log.d(TAG, "received")
        if(p1?.action.equals(Intent.ACTION_BOOT_COMPLETED)){
            Log.d(TAG,"boot completed")
            val db = DatabaseHelper(p0!!)
            val cursor = db.getActiveGoalsData(false)
            val d = Calendar.getInstance().time

            while(cursor.moveToNext()){
                val goalDate = cursor.getString(3)
                val goalTime = cursor.getString(4)
                val dateTime = findDateTime(goalDate, goalTime)
                Log.d("TAG:", "dateTime $dateTime  d.time ${d.time}")
                if(dateTime > d.time) {

                    val goalId = cursor.getInt(0)
                    val goalName = cursor.getString(1)


                    val pendingIntent = createPendingIntent(p0, goalId, "goal", goalName, goalId)

                    createAlarmManager(p0, pendingIntent, dateTime)

                    val cpCursor = db.getAllCheckpointsOfGoal(goalId)
                    while (cpCursor.moveToNext()) {
                        val cpId = cpCursor.getInt(0)
                        val cpDate = cpCursor.getString(3)
                        val cpTime = cpCursor.getString(4)
                        val cpPendingIntent = createPendingIntent(p0, cpId, "checkpoint", goalName, goalId)
                        val cpDateTime = findDateTime(cpDate, cpTime)
                        createAlarmManager(p0, cpPendingIntent, cpDateTime)
                    }
                }

            }

        }


        else {
            val notificationHelper = NotificationHelper(p0!!)

            if (p1?.getStringExtra("type") == "goal") {
                Log.d(TAG, "type = goal")
                notificationHelper.getManager()
                    .notify(
                        p1.getIntExtra("code", 0),
                        notificationHelper.createGoalReminderNotification(
                            p1.getStringExtra("goal_name"), p1.getIntExtra("code", 0)
                        ).build()
                    )
            }
            else if (p1?.getStringExtra("type") == "checkpoint") {
                Log.d(TAG, "type = checkpoint")
                notificationHelper.getManager().notify(
                    p1!!.getIntExtra("code", 0),
                    notificationHelper.createCheckpointReminderNotification(
                        p1.getStringExtra("goal_name"),
                        p1.getIntExtra("code", 0), p1.getIntExtra("goal_id", 0)
                    ).build()
                )
            }

        }
    }

    private fun findDateTime(goalDate: String?, goalTime: String?): Long {
        if(goalDate == null || goalTime == null)
            return 0L

        val dateArr = goalDate.split("-")
        val timeArr = goalTime.split(":")

        val dateTimeToAlarm = Calendar.getInstance(Locale.getDefault())
        dateTimeToAlarm.set(Calendar.YEAR, dateArr[0].toInt())
        dateTimeToAlarm.set(Calendar.MONTH, (dateArr[1].toInt() -1) )
        dateTimeToAlarm.set(Calendar.DAY_OF_MONTH, dateArr[2].toInt())
        dateTimeToAlarm.set(Calendar.HOUR_OF_DAY, timeArr[0].toInt())
        dateTimeToAlarm.set(Calendar.MINUTE, timeArr[1].toInt())
        dateTimeToAlarm.set(Calendar.SECOND, 0)
        return dateTimeToAlarm.timeInMillis
    }

    companion object{
        fun createAlarmManager(context: Context, pendingIntent: PendingIntent,time:Long){
            Log.d("AlarmReceiver", "companion CAM: time is " + time)
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            //am.setExact(AlarmManager.RTC_WAKEUP, time, pendingIntent)
            am.setRepeating(AlarmManager.RTC_WAKEUP, time, time,pendingIntent)
        }

        fun createPendingIntent(context: Context, resultCode:Int, typeValue:String, goalName:String, goalId: Int) : PendingIntent{
            Log.d("AlarmReceiver","code $resultCode  type $typeValue  name $goalName")
            val notifyIntent = Intent(context.applicationContext, AlarmReceiver::class.java)
            notifyIntent.putExtra("goal_name",goalName)
            notifyIntent.putExtra("type",typeValue)
            notifyIntent.putExtra("code",resultCode)
            notifyIntent.putExtra("goal_id", goalId)
            return PendingIntent.getBroadcast(context.applicationContext, resultCode,
                notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        }
    
}