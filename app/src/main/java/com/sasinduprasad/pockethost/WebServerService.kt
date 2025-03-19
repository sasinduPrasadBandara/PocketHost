package com.sasinduprasad.androidserver

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.activity.viewModels
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import com.sasinduprasad.pockethost.AndroidWebServer
import com.sasinduprasad.pockethost.MainActivity
import com.sasinduprasad.pockethost.R
import com.sasinduprasad.pockethost.ServerViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.IOException

class WebServerService : Service() {
    private var server: AndroidWebServer? = null
    private val NOTIFICATION_ID = 1
    private val CHANNEL_ID = "WebServerChannel"

    object ServerViewModelSingleton {
        val viewModel = ServerViewModel()
    }

    val viewModel = ServerViewModelSingleton.viewModel



    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "START" -> startServer()
            "STOP" -> stopServer()
        }

        return START_STICKY
    }

@SuppressLint("ForegroundServiceType")
private fun startServer() {
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        if (server == null) {
            val port = 8080
            if (!isPortAvailable(port)) {
                stopForeground(true)
                stopSelf()
                return
            }

            server = AndroidWebServer(port,viewModel)
            try {
                server?.start()
                viewModel.setServerStatus(true)
//                applicationContext.sendBroadcast(
//                    Intent("SERVER_STATUS").putExtra("RUNNING", true)
//                )
            } catch (e: IOException) {
                e.printStackTrace()
                stopSelf()
            }
        }
    }

    // Utility function to check if port is free
    private fun isPortAvailable(port: Int): Boolean {
        return try {
            val socket = java.net.ServerSocket(port)
            socket.close()
            true
        } catch (e: IOException) {
            false
        }
    }


    private fun stopServer() {
        server?.stop()
        viewModel.setServerStatus(false)
//        applicationContext.sendBroadcast(
//            Intent("SERVER_STATUS").putExtra("RUNNING", false)
//        )
        server = null
        stopSelf()
    }



    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        stopServer()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Web Server Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Web Server Running")
            .setContentText("Server running on port 8080")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .build()
    }
}