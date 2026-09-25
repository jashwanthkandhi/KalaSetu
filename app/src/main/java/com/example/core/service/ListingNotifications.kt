package com.example.core.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R

object ListingNotifications {
    fun show(context: Context, id: String, title: String, message: String, kind: String) {
        val prefs = context.getSharedPreferences("kalasetu_prefs", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("notify_$kind", true)) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) manager.createNotificationChannel(
            NotificationChannel("listings", "Listing updates", NotificationManager.IMPORTANCE_DEFAULT))
        val intent = PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        try {
            NotificationManagerCompat.from(context).notify(id.hashCode(), NotificationCompat.Builder(context, "listings")
                .setSmallIcon(R.drawable.ic_launcher_foreground).setContentTitle(title).setContentText(message)
                .setContentIntent(intent).setAutoCancel(true).setOnlyAlertOnce(true).build())
        } catch (_: SecurityException) { /* Notifications are optional; work remains available in the catalog. */ }
    }
}
