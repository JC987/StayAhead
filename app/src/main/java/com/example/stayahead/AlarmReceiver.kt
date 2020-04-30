package com.example.stayahead

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class AlarmReceiver : BroadcastReceiver() {
    val TAG:String = "AlarmReceiver"
    override fun onReceive(p0: Context?, p1: Intent?) {
        Log.d(TAG, "received")
        val notificationHelper = NotificationHelper(p0!!)

        if(p1?.getStringExtra("type") == "goal") {
            Log.d(TAG, "type = goal")
            notificationHelper.getManager()
                .notify(p1.getIntExtra("code",0), notificationHelper.createGoalReminderNotification(
                    p1.getStringExtra("goal_name"),p1.getIntExtra("code",0)).build())
        }else {
            Log.d(TAG, "type = checkpoint")
            notificationHelper.getManager().notify(p1!!.getIntExtra("code",0) + 100000,
                notificationHelper.createCheckpointReminderNotification(p1.getStringExtra("goal_name"),
                    p1.getIntExtra("code",0),p1.getIntExtra("goal_id",0)).build()
            )
        }
    }
}