package com.example.work_1

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.widget.RemoteViews
import androidx.annotation.RawRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData


class MusicService : Service() {
    private var mPlayer: MediaPlayer = MediaPlayer()
    private var nextMusicShouldBePlayed: Boolean = false
    private var currentMusicId: Int? = null
    private val binder = MusicBinder()
    val isPlaying: Boolean
        get() = mPlayer.isPlaying

    private val _currentMusicTitle = MutableLiveData("")
    val currentMusicTitle: LiveData<String> = _currentMusicTitle


    private val listMusic = listOf(
        R.raw.m1,
        R.raw.m2,
        R.raw.m3,
    )

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        createMediaPlayer(listMusic.first())
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PAUSE -> pause()
            ACTION_PLAY -> play()
            ACTION_NEXT -> setNextMusic()
            ACTION_PREVIOUS -> setPreviousMusic()
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        mPlayer.release()
    }

    private fun createMediaPlayer(@RawRes musicId: Int) {
        mPlayer.release()

        mPlayer = MediaPlayer.create(this, musicId).apply {
            setOnCompletionListener {
                setNextMusic()
            }
        }
        if (nextMusicShouldBePlayed) play()

        currentMusicId = musicId
        _currentMusicTitle.value = "Composition $musicId"
    }

    @SuppressLint("MissingPermission")
    fun startForeground() {
        val notification = createNotification()
        // it's called manually, because this way the notification appears faster
        NotificationManagerCompat
            .from(this)
            .notify(PLAYER_NOTIFICATION_ID, notification)
        startForeground(PLAYER_NOTIFICATION_ID, notification)
    }

    fun stopForeground() {
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun createNotification(): Notification {
        val playOrPauseIntent = if (isPlaying) {
            Intent(this, MusicService::class.java).apply {
                action = ACTION_PAUSE
            }
        } else {
            Intent(this, MusicService::class.java).apply {
                action = ACTION_PLAY
            }
        }

        val nextIntent = Intent(this, MusicService::class.java).apply {
            action = ACTION_NEXT
        }

        val previousIntent = Intent(this, MusicService::class.java).apply {
            action = ACTION_PREVIOUS
        }

        val notificationLayout = RemoteViews(packageName, R.layout.notification_player)
            .apply {
                setOnClickPendingIntent(
                    R.id.notification_imageButton_playOrPause,
                    getPendingIntent(playOrPauseIntent)
                )
                setOnClickPendingIntent(
                    R.id.notification_imageButton_next,
                    getPendingIntent(nextIntent)
                )
                setOnClickPendingIntent(
                    R.id.notification_imageButton_previous,
                    getPendingIntent(previousIntent)
                )
                setTextViewText(
                    R.id.notification_textView_title,
                    _currentMusicTitle.value,
                )
                setImageViewResource(
                    R.id.notification_imageButton_next,
                    R.drawable.baseline_next,
                )
                setImageViewResource(
                    R.id.notification_imageButton_previous,
                    R.drawable.baseline_previous,
                )
                setImageViewResource(
                    R.id.notification_imageButton_playOrPause,
                    if (isPlaying) R.drawable.baseline_pause
                    else R.drawable.baseline_play
                )
            }

        return NotificationCompat
            .Builder(this, NotificationChannels.MUSIC_CHANNEL_ID)
            .setContentTitle("Music Player")
            .setSmallIcon(R.drawable.baseline_music)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(notificationLayout)
            .build()
    }

    private fun getPendingIntent(intent: Intent): PendingIntent? {
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_MUTABLE)
    }

    fun play() {
        nextMusicShouldBePlayed = true
        mPlayer.start()
        refreshPlayerNotification()
    }

    fun pause() {
        nextMusicShouldBePlayed = false
        mPlayer.pause()
        refreshPlayerNotification()
    }

    @SuppressLint("MissingPermission")
    private fun refreshPlayerNotification() {
        val mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        for (notification in mNotificationManager.activeNotifications) {
            if (notification.id == PLAYER_NOTIFICATION_ID) {
                val newNotification = createNotification()
                NotificationManagerCompat
                    .from(this)
                    .notify(PLAYER_NOTIFICATION_ID, newNotification)
            }
        }
    }

    fun setNextMusic() {
        val currentIndex = listMusic.indexOf(currentMusicId)
        val nextIndex =
            if (currentIndex == listMusic.size - 1) 0
            else currentIndex + 1
        createMediaPlayer(listMusic[nextIndex])
        refreshPlayerNotification()
    }

    fun setPreviousMusic() {
        val currentIndex = listMusic.indexOf(currentMusicId)
        val previousIndex =
            if (currentIndex == 0) listMusic.size - 1
            else currentIndex - 1
        createMediaPlayer(listMusic[previousIndex])
        refreshPlayerNotification()
    }

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    companion object {
        private const val PLAYER_NOTIFICATION_ID = 13535

        private const val ACTION_PLAY = "ACTION_PLAY"
        private const val ACTION_PAUSE = "ACTION_PAUSE"
        private const val ACTION_NEXT = "ACTION_NEXT"
        private const val ACTION_PREVIOUS = "ACTION_PREVIOUS"
    }
}