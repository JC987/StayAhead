package com.example.stayahead

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        Log.d("NotificationHelper", "receiver")
        val n = NotificationHelper(p0!!)

        if(p1?.getStringExtra("type") == "goal")
            n.getManager().notify(0,n.createGoalReminderNotification(p1.getStringExtra("goal_name")).build())
        else
            n.getManager().notify(1, n.createCheckpointReminderNotification(p1?.getStringExtra("goal_name")).build())

    }
}