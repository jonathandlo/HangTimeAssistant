package com.example.hangtimeassistant

import android.graphics.Color
import java.lang.NullPointerException
import java.util.*

object Model {
    var reminders = mutableMapOf<Int , Reminder>()
    var contacts = mutableMapOf<Int , Contact>()
    var events = mutableMapOf<Int , Event>()
    var categories = mutableMapOf<Int , Category>()
}

object IDGen {
    private var currentID = 0
    fun nextID():Int {
       return currentID++
    }
}

data class Reminder (
    // keys
    var ID: Int = 0,
    var ContactID: Int = 0,
    var EventID: Int = 0,

    // attributes
    var lastDate: Date = Date(0),
    var nextDate: Date = Date(0),
    var reminder: Boolean = false,
    var reminderPeriod: Int = 1,
    var reminderUnit: String = "d",
    var random: Boolean = false,
    var randomPeriod: Int = 0,
    var randomUnit: String = "d"
)

data class Contact (
    // keys
    var ID: Int = 0,
    var ReminderID: Int = 0,
    var CategoryIDs: MutableList<Int> = mutableListOf(),
    var EventIDs: MutableList<Int> = mutableListOf(),

    // attributes
    var name: String = "",
    var IGUrl: String = "",
    var FBUrl: String = "",
    var phoneNum: String = "",
    var address: String = ""
)

data class Event (
    // keys
    var ID: Int = 0,
    var ReminderID: Int = 0,
    var ContactIDs: MutableList<Int> = mutableListOf(),
    var CategoryIDs: MutableList<Int> = mutableListOf(),

    // attributes
    var date: Date = Date(0),
    var name: String = "",
    var description: String = "",
    var address: String = ""
)

data class Category (
    // keys
    var ID: Int = 0,
    var ContactIDs: MutableList<Int> = mutableListOf(),
    var EventIDs: MutableList<Int> = mutableListOf(),

    // attributes
    var color: Int = Color.argb(255, 55, 55, 55),
    var name: String = ""
)