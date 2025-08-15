
package dev.notifyninja.reminders

import androidx.room.*

enum class Frequency { NONE, DAILY, WEEKLY, MONTHLY }

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String,
    val triggerAt: Long,
    val frequency: Frequency
)

@Dao
interface ReminderDao {
    @Insert suspend fun insert(reminder: Reminder): Long
    @Query("SELECT * FROM reminders WHERE id=:id") suspend fun getById(id: Int): Reminder?
    @Query("SELECT * FROM reminders") suspend fun all(): List<Reminder>
    @Delete suspend fun delete(reminder: Reminder)
    @Update suspend fun update(reminder: Reminder)
}

@Database(entities = [Reminder::class], version = 1)
abstract class AppDb: RoomDatabase() {
    abstract fun dao(): ReminderDao
    companion object {
        @Volatile private var INSTANCE: AppDb? = null
        fun get(ctx: android.content.Context): AppDb =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: androidx.room.Room.databaseBuilder(
                    ctx.applicationContext, AppDb::class.java, "notify-ninja.db"
                ).build().also { INSTANCE = it }
            }
    }
}
