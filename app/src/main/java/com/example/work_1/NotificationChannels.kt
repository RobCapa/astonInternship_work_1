package com.example.work_1

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat

object NotificationChannels {
    const val MUSIC_CHANNEL_ID = "music_channel_id"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createMusicChannel(context)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createMusicChannel(context: Context) {
        val nameChannel = "Music Player"
        val descriptionChannel = "Show music player"
        val priorityChannel = NotificationManager.IMPORTANCE_HIGH

        val channel = NotificationChannel(
            MUSIC_CHANNEL_ID,
            nameChannel,
            priorityChannel,
        ).apply {
            description = descriptionChannel
            setSound(null, null)
            enableVibration(false)
        }

        NotificationManagerCompat.from(context).createNotificationChannel(channel)
    }
}