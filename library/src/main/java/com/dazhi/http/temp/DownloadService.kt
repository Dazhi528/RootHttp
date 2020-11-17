package com.dazhi.http.temp

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.dazhi.http.R
import com.dazhi.http.upgrade.DialogDownloadTask


class DownloadService : Service() {
    private var mDownloadTask: DialogDownloadTask? = null

    private val mDownloadListener = object : DownloadListener {
        override fun onProgress(progress: Int) {
            (getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager)?.notify(1,
                    createNotification("Downloading...", progress))
        }

        override fun onSuccess() {
            mDownloadTask = null
            (getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager)?.notify(1,
                    createNotification("Download Success", -1))
        }

        override fun onFailed() {
            mDownloadTask = null
            stopForeground(true)
            (getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager)?.notify(1,
                    createNotification("Download Failed", -1))
        }

        override fun onPaused() {
            mDownloadTask=null
        }

        override fun onCanceled() {
            mDownloadTask=null
            stopForeground(true)
        }
    }

    // 创建通知
    private fun createNotification(title: String, progress: Int): Notification {
        val CHANNEL_ID = "channel_id"
        // 兼容处理
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel: NotificationChannel? = NotificationChannel(CHANNEL_ID,
                    "channel_id_name", NotificationManager.IMPORTANCE_HIGH)
            if(notificationChannel!=null) {
                val notificationManager = getSystemService(NOTIFICATION_SERVICE) as? NotificationManager
                notificationManager?.createNotificationChannel(notificationChannel)
            }
        }
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
//                .setSmallIcon(R.drawable.ic_notification)
        if (progress > 0) {
            builder.setContentText("${progress}%")
                    .setProgress(100, progress, false)
        }
        return builder.build()
    }

    override fun onBind(intent: Intent): IBinder {
        return DownloadBinder()
    }

    inner class DownloadBinder : Binder() {
        fun startDownload(boReLoad: Boolean, url: String, saveDir: String) {
            if (mDownloadTask == null) {
                mDownloadTask = DialogDownloadTask(mDownloadListener)
                mDownloadTask!!.execute(boReLoad, url, saveDir)
                startForeground(1, createNotification("Download...", 0))
            }
        }
        fun pauseDownload() {
            mDownloadTask?.pause()
        }
        fun cancelDownload() {
            if(mDownloadTask!=null) {
                mDownloadTask!!.cancel()
            }else {
                (getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager)?.cancel(1)
                stopForeground(true)
            }
        }
    }

}
