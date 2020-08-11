package com.example.hangtimeassistant

import android.graphics.Color
import androidx.room.*

//
// Daos
//

@Dao
interface ReminderDao {
    // Set functions
    @Insert fun insert(pItem: Reminder): Long
    @Update fun update(vararg pItem: Reminder): Int
    @Delete fun delete(vararg pItem: Reminder): Int

    // Get functions
    @Query("SELECT * FROM tbl_reminder")
    fun loadReminders(): List<Reminder>
}

@Dao
interface ContactDao {
    // Set functions
    @Insert fun insert(pItem: Contact): Long
    @Update fun update(vararg pItem: Contact): Int
    @Delete fun delete(vararg pItem: Contact): Int
    @Query("INSERT INTO contact2category (contactID, categoryID)" +
            "VALUES (:pContactID, :pCategoryID)")
    fun linkToCategory(pContactID: Long, pCategoryID: Long)

    // Get functions
    @Query("SELECT * FROM tbl_contact")
    fun loadContacts(): List<Contact>
    @Query("SELECT * FROM tbl_category " +
            "INNER JOIN contact2category ON tbl_category.ID = contact2category.categoryID " +
            "WHERE contactID = :pContactID")
    fun loadCategories(pContactID: Long): List<Category>
    @Query("SELECT * FROM tbl_event " +
            "INNER JOIN contact2event ON tbl_event.ID = contact2event.eventID " +
            "WHERE contactID = :pContactID")
    fun loadEvents(pContactID: Long): List<Event>
}

@Dao
interface EventDao {
    // Set functions
    @Insert fun insert(pItem: Event): Long
    @Update fun update(vararg pItem: Event): Int
    @Delete fun delete(vararg pItem: Event): Int

    // Get functions
    @Query("SELECT * FROM tbl_event")
    fun loadEvents(): List<Event>
}

@Dao
interface CategoryDao {
    // Set functions
    @Insert fun insert(pItem: Category): Long
    @Update fun update(vararg pItem: Category): Int
    @Delete fun delete(vararg pItem: Category): Int

    // Get functions
    @Query("SELECT * FROM tbl_category")
    fun loadCategories(): List<Category>
}

//
// Relationship tables
//

@Entity(tableName = "contact2category")
data class Contact2Category(
    @PrimaryKey(autoGenerate = true) var ID: Long= 0,
    @ColumnInfo val contactID: Int,
    @ColumnInfo val categoryID: Int
)

@Entity(tableName = "contact2event")
data class Contact2Event(
    @PrimaryKey(autoGenerate = true) var ID: Int,
    @ColumnInfo val contactID: Int,
    @ColumnInfo val eventID: Int
)

@Entity(tableName = "event2category")
data class Event2Category(
    @PrimaryKey(autoGenerate = true) var ID: Int,
    @ColumnInfo val eventID: Int,
    @ColumnInfo val categoryID: Int
)

//
// Entity tables
//

@Entity(tableName = "tbl_reminder")
data class Reminder (
    // keys
    @PrimaryKey(autoGenerate = true) var ID: Long = 0,
    @ColumnInfo var ContactID: Long = 0,
    @ColumnInfo var EventID: Long = 0,

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
    @PrimaryKey(autoGenerate = true) var ID: Long = 0,
    @ColumnInfo var ReminderID: Long = 0,
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
    @PrimaryKey(autoGenerate = true) var ID: Long = 0,
    @ColumnInfo var ReminderID: Long = 0,
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
    @PrimaryKey(autoGenerate = true) var ID: Long = 0,
    //@ColumnInfo var ContactIDs: MutableList<Int> = mutableListOf(),
    //@ColumnInfo var EventIDs: MutableList<Int> = mutableListOf(),

    // attributes
    @ColumnInfo var color: Int = Color.argb(255, 55, 55, 55),
    @ColumnInfo var name: String = ""
)