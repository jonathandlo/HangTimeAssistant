package com.example.hangtimeassistant

import android.content.Context
import android.database.Cursor
import android.text.TextUtils
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.sql.SQLException


@Database(
    entities = [
        Contact::class,
        Event::class,
        Category::class,
        Contact2Category::class,
        Contact2Event::class,
        Event2Category::class],
    version = 3,
    exportSchema = false
)
abstract class HangTimeDB : RoomDatabase() {

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
                val migration_2_3 = object : Migration(2, 3) {
                    override fun migrate(database: SupportSQLiteDatabase) {
                        database.execSQL("DROP TABLE tbl_reminder")
                        dropColumn(database,
                            "CREATE TABLE tbl_contact(" +
                                    "ID INTEGER PRIMARY KEY," +
                                    "name TEXT," +
                                    "IGUrl TEXT," +
                                    "FBUrl TEXT," +
                                    "phoneNum TEXT," +
                                    "address TEXT," +
                                    "reminder INTEGER," +
                                    "reminderCadence INTEGER," +
                                    "reminderCadenceUnit TEXT," +
                                    "reminderDelay INTEGER," +
                                    "reminderDelayAmount INTEGER," +
                                    "reminderDelayUnit TEXT" +
                                    ")",
                            "tbl_contact", arrayOf("ReminderID"))
                        dropColumn(database,
                            "CREATE TABLE tbl_event(" +
                                    "ID INTEGER PRIMARY KEY," +
                                    "date INTEGER," +
                                    "name TEXT," +
                                    "description TEXT," +
                                    "address TEXT" +
                                    ")",
                            "tbl_event", arrayOf("ReminderID"))
                    }
                }

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HangTimeDB::class.java,
                    "hangtime_db"
                )
                    .allowMainThreadQueries()
                    .addMigrations(migration_2_3)
                    .build()
                INSTANCE = instance
                return instance
            }
        }

        @Throws(SQLException::class)
        private fun dropColumn(db: SupportSQLiteDatabase, createTableCmd: String, tableName: String, colsToRemove: Array<String>) {
            val updatedTableColumns: MutableList<String> = getTableColumns(db, tableName)
            // Remove the columns we don't want anymore from the table's list of columns
            updatedTableColumns.removeAll(colsToRemove)
            val columnsSeperated = TextUtils.join(",", updatedTableColumns)
            db.execSQL("ALTER TABLE " + tableName + " RENAME TO " + tableName + "_old;")

            // Creating the table on its new format (no redundant columns)
            db.execSQL(createTableCmd)

            // Populating the table with the data
            db.execSQL("INSERT INTO " + tableName + "(" + columnsSeperated + ") SELECT "
                        + columnsSeperated + " FROM " + tableName + "_old;"
            )
            db.execSQL("DROP TABLE " + tableName + "_old;")
        }
        fun getTableColumns(db : SupportSQLiteDatabase, tableName: String): MutableList<String> {
            val columns = ArrayList<String>()
            val cmd = "pragma table_info($tableName);"
            val cur: Cursor = db.query(cmd, null)
            while (cur.moveToNext()) {
                columns.add(cur.getString(cur.getColumnIndex("name")))
            }
            cur.close()
            return columns
        }
    }
}