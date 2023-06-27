package com.example.work_1

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.work_1.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by viewBinding()

    private lateinit var mService: MusicService
    private var isServiceConnected: Boolean = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as MusicService.MusicBinder
            mService = binder.getService()
            isServiceConnected = true
            mService.currentMusicTitle.observe(this@MainActivity) {
                binding.mainActivityTextViewTitle.text = it
            }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isServiceConnected = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intentBind = Intent(this, MusicService::class.java)
        bindService(intentBind, connection, Context.BIND_AUTO_CREATE)

        setContentView(R.layout.activity_main)
        setOnClickListeners()
    }

    override fun onStart() {
        super.onStart()
        if (this::mService.isInitialized) {
            mService.stopForeground()
            isServiceConnected = true
        }
    }

    private fun setOnClickListeners() {
        binding.mainActivityImageButtonPlayOrPause.setOnClickListener {
            if (!isServiceConnected) return@setOnClickListener

            if (mService.isPlaying) {
                mService.pause()
                binding.mainActivityImageButtonPlayOrPause.setImageResource(R.drawable.baseline_play)
            } else {
                mService.play()
                binding.mainActivityImageButtonPlayOrPause.setImageResource(R.drawable.baseline_pause)
            }
        }

        binding.mainActivityImageButtonNext.setOnClickListener {
            if (!isServiceConnected) return@setOnClickListener
            mService.setNextMusic()
        }
        binding.mainActivityImageButtonPrevious.setOnClickListener {
            if (!isServiceConnected) return@setOnClickListener
            mService.setPreviousMusic()
        }
    }

    override fun onStop() {
        super.onStop()
        isServiceConnected = false
        mService.startForeground()
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(connection)
    }
}