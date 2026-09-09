package com.islamic.bangla.data.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.islamic.bangla.MainActivity
import com.islamic.bangla.R

/** Builds the notification channel + prayer-time notifications. */
object PrayerNotificationHelper {

    const val CHANNEL_ID = "prayer_times"

    fun showPrayerNotification(context: Context, prayerKey: String, prayerBanglaName: String) {
        createChannel(context)

        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notifId = prayerKey.hashCode()
        val stopIntent = Intent(context, StopAzanReceiver::class.java).apply {
            putExtra(StopAzanReceiver.EXTRA_NOTIF_ID, notifId)
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            context, notifId, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("নামাজের সময়")
            .setContentText("$prayerBanglaName নামাজের সময় হয়েছে")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_notification, "থামান", stopPendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(notifId, notification)
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < 26) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "নামাজের সময়",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "ওয়াক্তের শুরুতে নামাজের নোটিফিকেশন"
        }
        context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }
}
