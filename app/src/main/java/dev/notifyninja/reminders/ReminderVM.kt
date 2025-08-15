
package dev.notifyninja.reminders

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReminderViewModel(app: Application): AndroidViewModel(app) {
    private val dao = AppDb.get(app).dao()
    private val scheduler = ReminderScheduler(app)

    suspend fun save(r: Reminder) {
        val id = withContext(Dispatchers.IO) { dao.insert(r).toInt() }
        scheduler.schedule(id)
    }
}
