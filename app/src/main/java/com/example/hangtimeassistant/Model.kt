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

//
// Daos
//

@Dao
interface ReminderDao {
    // Edit functions
    @Insert fun insert(vararg pItem: Reminder)
    @Update fun update(vararg pItem: Reminder): Int
    @Delete fun delete(vararg pItem: Reminder): Int
}

@Dao
interface ContactDao {
    // Edit functions
    @Insert fun insert(vararg pItem: Contact)
    @Update fun update(vararg pItem: Contact): Int
    @Delete fun delete(vararg pItem: Contact): Int

    // Retrieve functions
    @Query("SELECT * FROM tbl_contact")
    fun loadContacts(): List<Contact>

    @Query("SELECT * FROM tbl_category " +
            "INNER JOIN contact2category ON tbl_category.ID = contact2category.categoryID " +
            "WHERE contactID = :pContactID")
    fun loadCategories(pContactID: Int): List<Category>

    @Query("SELECT * FROM tbl_event " +
            "INNER JOIN contact2event ON tbl_event.ID = contact2event.eventID " +
            "WHERE contactID = :pContactID")
    fun loadEvents(pContactID: Int): List<Event>
}

@Dao
interface EventDao {
    @Insert fun insert(vararg pItem: Event)
    @Update fun update(vararg pItem: Event): Int
    @Delete fun delete(vararg pItem: Event): Int
}

@Dao
interface CategoryDao {
    @Insert fun insert(vararg pItem: Category)
    @Update fun update(vararg pItem: Category): Int
    @Delete fun delete(vararg pItem: Category): Int
}


//
// Entity tables
//

@Entity(tableName = "tbl_reminder")
data class Reminder (
    // keys
    @PrimaryKey var ID: Int = 0,
    @ColumnInfo var ContactID: Int = 0,
    @ColumnInfo var EventID: Int = 0,

    // attributes
    @ColumnInfo var lastDate: Long = 0,
    @ColumnInfo var nextDate: Long = 0,
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
    //@ColumnInfo var CategoryIDs: MutableList<Int> = mutableListOf(),
    //@ColumnInfo var EventIDs: MutableList<Int> = mutableListOf(),

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
    //@ColumnInfo var ContactIDs: MutableList<Int> = mutableListOf(),
    //@ColumnInfo var CategoryIDs: MutableList<Int> = mutableListOf(),

    // attributes
    @ColumnInfo var date: Long = 0,
    @ColumnInfo var name: String = "",
    @ColumnInfo var description: String = "",
    @ColumnInfo var address: String = ""
)

@Entity(tableName = "tbl_category")
data class Category (
    // keys
    @PrimaryKey var ID: Int = 0,
    //@ColumnInfo var ContactIDs: MutableList<Int> = mutableListOf(),
    //@ColumnInfo var EventIDs: MutableList<Int> = mutableListOf(),

    // attributes
    @ColumnInfo var color: Int = Color.argb(255, 55, 55, 55),
    @ColumnInfo var name: String = ""
)