package com.example.hangtimeassistant

import android.graphics.Color
import androidx.room.*
import java.util.*

object Model {
    var reminders = mutableMapOf<Int , Reminder>()
    var contacts = mutableMapOf<Int , Contact>()
    var events = mutableMapOf<Int , Event>()
    var categories = mutableMapOf<Int , Category>()
}

object IDGen {
    private var currentID = 0
    fun nextID(): Int {
       return currentID++
    }
}

@Dao
interface ReminderDao {
    @Insert fun insert(vararg pReminders: Reminder)
    @Update fun update(vararg pReminders: Reminder): Int
    @Delete fun delete(vararg pReminders: Reminder): Int
}

@Entity(tableName = "tbl_reminder")
data class Reminder (
    // keys
    @PrimaryKey var ID: Int = 0,
    @ColumnInfo var ContactID: Int = 0,
    @ColumnInfo var EventID: Int = 0,

    // attributes
    @ColumnInfo var lastDate: Date = Date(0),
    @ColumnInfo var nextDate: Date = Date(0),
    @ColumnInfo var reminder: Boolean = false,
    @ColumnInfo var reminderPeriod: Int = 1,
    @ColumnInfo var reminderUnit: String = "d",
    @ColumnInfo var random: Boolean = false,
    @ColumnInfo var randomPeriod: Int = 0,
    @ColumnInfo var randomUnit: String = "d"
)

@Entity(tableName = "tbl_contact")
data class Contact (
    // keys
    @PrimaryKey var ID: Int = 0,
    @ColumnInfo var ReminderID: Int = 0,
    @ColumnInfo var CategoryIDs: MutableList<Int> = mutableListOf(),
    @ColumnInfo var EventIDs: MutableList<Int> = mutableListOf(),

    // attributes
    @ColumnInfo var name: String = "",
    @ColumnInfo var IGUrl: String = "",
    @ColumnInfo var FBUrl: String = "",
    @ColumnInfo var phoneNum: String = "",
    @ColumnInfo var address: String = ""
)

@Entity(tableName = "tbl_event")
data class Event (
    // keys
    @PrimaryKey var ID: Int = 0,
    @ColumnInfo var ReminderID: Int = 0,
    @ColumnInfo var ContactIDs: MutableList<Int> = mutableListOf(),
    @ColumnInfo var CategoryIDs: MutableList<Int> = mutableListOf(),

    // attributes
    @ColumnInfo var date: Date = Date(0),
    @ColumnInfo var name: String = "",
    @ColumnInfo var description: String = "",
    @ColumnInfo var address: String = ""
)

@Entity(tableName = "tbl_category")
data class Category (
    // keys
    @PrimaryKey var ID: Int = 0,
    @ColumnInfo var ContactIDs: MutableList<Int> = mutableListOf(),
    @ColumnInfo var EventIDs: MutableList<Int> = mutableListOf(),

    // attributes
    @ColumnInfo var color: Int = Color.argb(255, 55, 55, 55),
    @ColumnInfo var name: String = ""
)