package com.example.stayahead

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        Log.d("NotificationHelper", "receiver")
        val n = NotificationHelper(p0!!)

        if(p1?.getStringExtra("type") == "goal") {
            Log.d("NotificationHelper", "type = goal")
            n.getManager()
                .notify(p1.getIntExtra("code",0), n.createGoalReminderNotification(p1.getStringExtra("goal_name"),p1.getIntExtra("code",0)).build())
        }else {
            Log.d("NotificationHelper", "type = checkpoint")
            n.getManager().notify(
                p1!!.getIntExtra("code",0) + 100000,
                n.createCheckpointReminderNotification(p1.getStringExtra("goal_name"),p1.getIntExtra("code",0),p1.getIntExtra("goal_id",0)).build()
            )
        }
    }
}