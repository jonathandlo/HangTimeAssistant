package com.example.hangtimeassistant

import android.content.Context
import androidx.room.*

@Database(entities = [Reminder::class, Contact::class, Event::class, Category::class], version = 1, exportSchema = false)
abstract class HangTimeDB : RoomDatabase() {

    abstract fun reminderDao(): ReminderDao
    abstract fun contactDao(): ContactDao
    abstract fun eventDao(): EventDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: HangTimeDB? = null

        fun getDatabase(context: Context): HangTimeDB {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HangTimeDB::class.java,
                    "hangtime_db"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}