package hypr.social.hangtimeassistant

import android.content.Context
import android.text.TextUtils
import androidx.room.*
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
                                    "ID INTEGER PRIMARY KEY NOT NULL DEFAULT 0," +
                                    "name TEXT NOT NULL DEFAULT ''," +
                                    "IGUrl TEXT NOT NULL DEFAULT ''," +
                                    "FBUrl TEXT NOT NULL DEFAULT ''," +
                                    "phoneNum TEXT NOT NULL DEFAULT ''," +
                                    "address TEXT NOT NULL DEFAULT ''," +
                                    "reminder INTEGER NOT NULL DEFAULT 0," +
                                    "reminderStartDate INTEGER NOT NULL DEFAULT 0," +
                                    "reminderCadence INTEGER NOT NULL DEFAULT 7," +
                                    "reminderCadenceUnit TEXT NOT NULL DEFAULT 'days'," +
                                    "reminderDelay INTEGER NOT NULL DEFAULT 0," +
                                    "reminderDelayAmount INTEGER NOT NULL DEFAULT 0," +
                                    "reminderDelayUnit TEXT NOT NULL DEFAULT 'days'" +
                                    ")",
                            "tbl_contact", arrayOf("ReminderID"))
                        dropColumn(database,
                            "CREATE TABLE tbl_event(" +
                                    "ID INTEGER PRIMARY KEY NOT NULL DEFAULT 0," +
                                    "date INTEGER NOT NULL DEFAULT 0," +
                                    "name TEXT NOT NULL DEFAULT ''," +
                                    "description TEXT NOT NULL DEFAULT ''," +
                                    "address TEXT NOT NULL DEFAULT ''" +
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
        private fun getTableColumns(db : SupportSQLiteDatabase, tableName: String): MutableList<String> {
            val columns = ArrayList<String>()
            val cmd = "pragma table_info($tableName);"
            val cur = db.query(cmd, null)
            while (cur.moveToNext()) {
                columns.add(cur.getString(cur.getColumnIndex("name")))
            }
            cur.close()
            return columns
        }
    }
}