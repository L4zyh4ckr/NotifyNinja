
package dev.notifyninja.reminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import dev.notifyninja.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class ReminderReceiver: BroadcastReceiver() {
    private val scope = CoroutineScope(Dispatchers.IO)
    
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            // Reschedule all reminders on boot
            scope.launch {
                val dao = AppDb.get(context).dao()
                dao.all().forEach { r ->
                    ReminderScheduler(context).schedule(r.id)
                }
            }
            return
        }

        val id = intent.getIntExtra("reminderId", -1)
        if (id == -1) return

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val chId = "reminders"
        if (Build.VERSION.SDK_INT >= 26) {
            nm.createNotificationChannel(
                NotificationChannel(chId, context.getString(R.string.channel_reminders), NotificationManager.IMPORTANCE_HIGH).apply {
                    description = context.getString(R.string.channel_desc)
                    enableLights(true)
                    lightColor = Color.BLUE
                    enableVibration(true)
                }
            )
        }

        scope.launch {
            val dao = AppDb.get(context).dao()
            val reminder = dao.getById(id) ?: return@launch
            
            // Get category color
            val categoryColorResId = when(reminder.category) {
                "Payments" -> R.color.category_payments
                "Classes" -> R.color.category_classes
                "Journeys" -> R.color.category_journeys
                "Exercise" -> R.color.category_exercise
                else -> R.color.category_other
            }
            val categoryColor = ContextCompat.getColor(context, categoryColorResId)
            
            val openManager = PendingIntent.getActivity(
                context, 1000 + id,
                Intent().apply {
                    setClassName(context, "dev.notifyninja.ui.ManagerActivity")
                    action = "dev.notifyninja.OPEN_MANAGER"
                    putExtra("reminder_id", id)
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            val deleteIntent = PendingIntent.getBroadcast(
                context, 2000 + id,
                Intent(context, ReminderReceiver::class.java).apply {
                    action = "dev.notifyninja.ACTION_DELETE_REMINDER"
                    putExtra("reminderId", id)
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notif = NotificationCompat.Builder(context, chId)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle(reminder.title)
                .setContentText("${reminder.category} reminder")
                .setColor(categoryColor)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setAutoCancel(true)
                .setContentIntent(openManager)
                .setDeleteIntent(deleteIntent)
                .addAction(R.drawable.ic_stat_name, context.getString(R.string.manage), openManager)
                .build()

            nm.notify(id, notif)

            // Reschedule if repeating
            val next = when(reminder.frequency) {
                Frequency.DAILY -> reminder.triggerAt + 24L*60*60*1000
                Frequency.WEEKLY -> reminder.triggerAt + 7L*24*60*60*1000
                Frequency.MONTHLY -> Calendar.getInstance().apply { timeInMillis = reminder.triggerAt; add(Calendar.MONTH,1) }.timeInMillis
                Frequency.NONE -> -1L
            }
            if (next > 0) {
                dao.update(reminder.copy(triggerAt = next))
                ReminderScheduler(context).schedule(reminder.id)
            }
        }
    }
}
