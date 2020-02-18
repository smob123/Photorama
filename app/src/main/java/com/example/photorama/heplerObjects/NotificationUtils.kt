package com.example.photorama.heplerObjects

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.photorama.R

/**
 * @author Sultan.
 * handles creating notification channels, and managers.
 */

class NotificationUtils(context: Context) : ContextWrapper(context) {
    // the channel's id and name
    private val CHANNEL_ID = "photoramaNotificationId"
    private val CHANNEL_NAME = "Photorama Notifications"
    // the notifications manager
    private lateinit var manager: NotificationManager

    init {
        // create a notification channel only if the android version is
        // oreo, or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel()
        }
    }

    /**
     * creates a notifications channel.
     */
    @TargetApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        val channel =
            NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
        channel.enableLights(true)
        channel.enableVibration(true)
        channel.lightColor = R.color.colorPrimary
        channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        getManager().createNotificationChannel(channel)
    }

    /**
     * returns the notifications manager.
     * @return the notifications manager
     */
    fun getManager(): NotificationManager {
        if (!::manager.isInitialized) {
            manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        }

        return manager
    }

    /**
     * return a notification builder with a given title, and body.
     * @param title the title of the notification
     * @param body the body of the notification
     *
     * @return a notification builder with a given title, and body
     */
    fun getChannelNotification(title: String, body: String): NotificationCompat.Builder {
        return NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.avatar)
    }
}
