package com.example.stayahead

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

class AlarmReceiver : BroadcastReceiver() {
    val TAG:String = "AlarmReceiver"
    override fun onReceive(p0: Context?, p1: Intent?) {
        if(p1?.action.equals(Intent.ACTION_BOOT_COMPLETED)){
            Toast.makeText(p0,"WTF",Toast.LENGTH_SHORT).show()
            Log.d("TAG:", "WTF!!!!!!!!!!!!!!!!!!!!!!!!!!! " + p1?.getStringExtra("goal_name"))
            //TODO:
            //get active goals
            //check if user wants noti
            //get and filter data from db
            // resend all alarm managers for each goal and checkpoint
        }
        else{
            Toast.makeText(p0,"IDFK",Toast.LENGTH_SHORT).show();
            Log.d("TAG:", "IDFK!!!!!!!!!!!!!!!!!!!!!!!!!!!")

        }
        Log.d(TAG, "received")
        val notificationHelper = NotificationHelper(p0!!)

        if(p1?.getStringExtra("type") == "goal") {
            Log.d(TAG, "type = goal")
            notificationHelper.getManager()
                .notify(p1.getIntExtra("code",0), notificationHelper.createGoalReminderNotification(
                    p1.getStringExtra("goal_name"),p1.getIntExtra("code",0)).build())
        }else if(p1?.getStringExtra("type") == "checkpoint") {
            Log.d(TAG, "type = checkpoint")
            notificationHelper.getManager().notify(p1!!.getIntExtra("code",0) + 100000,
                notificationHelper.createCheckpointReminderNotification(p1.getStringExtra("goal_name"),
                    p1.getIntExtra("code",0),p1.getIntExtra("goal_id",0)).build()
            )
        }
    }
}