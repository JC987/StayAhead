package com.example.stayahead

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat


class NotificationHelper(val context:Context): ContextWrapper(context) { // context.getSystemService(NotificationManager::class.java)
    private var manager:NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val CHANNEL_NAME_GOALS:String = "Goals"
    private val CHANNEL_NAME_GOALS_ID:String = "channel_id_goals"
    private val CHANNEL_NAME_CHECKPOINTS:String = "Checkpoints"
    private val CHANNEL_NAME_CHECKPOINTS_ID:String = "channel_id_goals_checkpoints"

    init {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createChannels()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun createChannels(){
        val channel1 = NotificationChannel(CHANNEL_NAME_GOALS_ID,CHANNEL_NAME_GOALS,NotificationManager.IMPORTANCE_HIGH)
        val channel2 = NotificationChannel(CHANNEL_NAME_CHECKPOINTS_ID,CHANNEL_NAME_CHECKPOINTS,NotificationManager.IMPORTANCE_HIGH)
        manager.createNotificationChannel(channel1)
        manager.createNotificationChannel(channel2)
    }

    fun getManager():NotificationManager{
        manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        return manager
    }

    fun createGoalReminderNotification(title:String?):NotificationCompat.Builder{
        val notificationBuilder = NotificationCompat.Builder(applicationContext,CHANNEL_NAME_GOALS_ID)
        notificationBuilder.setSmallIcon(R.drawable.ic_launcher_foreground)
        notificationBuilder.setContentText("You have a goal due today")
        notificationBuilder.setContentTitle("$title is due today")
        notificationBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH)
        Log.d("NotificationHelper","create goal notification")
        val notificationIntent = Intent(this, SideNavDrawer::class.java)
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        notificationBuilder.setContentIntent(contentIntent)


        return notificationBuilder
    }

    fun createCheckpointReminderNotification(title: String?):NotificationCompat.Builder {
        val notificationBuilder = NotificationCompat.Builder(applicationContext,CHANNEL_NAME_GOALS_ID)
        notificationBuilder.setSmallIcon(R.drawable.ic_launcher_foreground)
        notificationBuilder.setContentText("You have a checkpoint due today")
        notificationBuilder.setContentTitle("You'r checkpoint for $title is due today")
        notificationBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH)
        Log.d("NotificationHelper","create goal notification")
        val notificationIntent = Intent(this, SideNavDrawer::class.java)
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        notificationBuilder.setContentIntent(contentIntent)

        return notificationBuilder
    }


}