
package dev.notifyninja.reminders

import android.app.AlarmManager
import android.app.AlarmManager.AlarmClockInfo
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

class ReminderScheduler(private val ctx: Context) {
    private val dao = AppDb.get(ctx).dao()

    fun schedule(id: Int) {
        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = PendingIntent.getBroadcast(
            ctx, id,
            Intent(ctx, ReminderReceiver::class.java).apply {
                action = "dev.notifyninja.ACTION_FIRE_REMINDER"
                putExtra("reminderId", id)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        // Use AlarmClock to be exact without special permission
        val triggerAt = runBlockingQuery { dao.getById(id)?.triggerAt ?: System.currentTimeMillis()+60000 }
        val aci = AlarmClockInfo(triggerAt, pi)
        am.setAlarmClock(aci, pi)
    }

    private fun <T> runBlockingQuery(block: suspend () -> T): T {
        var result: T? = null
        val latch = java.util.concurrent.CountDownLatch(1)
        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            result = block()
            latch.countDown()
        }
        latch.await()
        @Suppress("UNCHECKED_CAST")
        return result as T
    }
}
