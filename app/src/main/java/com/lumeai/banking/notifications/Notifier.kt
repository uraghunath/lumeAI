package com.lumeai.banking.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.lumeai.banking.R

object Notifier {
	
	private const val CHANNEL_ID = "lumeai_updates"
	private const val CHANNEL_NAME = "LumeAI Updates"
	private const val CHANNEL_DESC = "Eligibility, DTI and next-best-action updates"
	
	fun ensureChannel(context: Context) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			val channel = NotificationChannel(
				CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT
			).apply { description = CHANNEL_DESC }
			val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
			nm.createNotificationChannel(channel)
		}
	}
	
	fun notify(context: Context, id: Int, title: String, text: String) {
		// Check notification permission for Android 13+
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			if (ContextCompat.checkSelfPermission(
					context,
					Manifest.permission.POST_NOTIFICATIONS
				) != PackageManager.PERMISSION_GRANTED
			) {
				// Permission not granted, skip notification
				android.util.Log.w("Notifier", "POST_NOTIFICATIONS permission not granted")
				return
			}
		}
		
		ensureChannel(context)
		val builder = NotificationCompat.Builder(context, CHANNEL_ID)
			.setSmallIcon(android.R.drawable.ic_dialog_info)
			.setContentTitle(title)
			.setContentText(text)
			.setStyle(NotificationCompat.BigTextStyle().bigText(text))
			.setAutoCancel(true)
		with(NotificationManagerCompat.from(context)) {
			notify(id, builder.build())
		}
	}
	
	fun eligibilityImproved(context: Context, old: Int, new: Int) {
		notify(context, 1001, "Eligibility improved", "Your eligibility rose from $old to $new. Check Dashboard.")
	}
	
	fun nextBestAction(context: Context, action: String) {
		notify(context, 1002, "Next best action", action)
	}
}


