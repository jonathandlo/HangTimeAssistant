package com.example.hangtimeassistant

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
    var date: Date = Date(0)
)

data class Contact (
    // keys
    var ID: Int = 0,
    var ReminderID: Int = 0,
    var CategoryIDs: List<Int> = emptyList(),

    // attributes
    var date: Date = Date(0)
)

data class Event (
    // keys
    var ID: Int = 0,
    var ReminderID: Int = 0,
    var ContactIDs: List<Int> = emptyList(),
    var CategoryIDs: List<Int> = emptyList(),

    // attributes
    var date: Date = Date(0)
)

data class Category (
    // keys
    var ID: Int = 0,
    var ContactIDs: List<Int> = emptyList(),
    var EventIDs: List<Int> = emptyList(),

    // attributes
    var date: Date = Date(0)
)