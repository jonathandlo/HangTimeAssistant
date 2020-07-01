package com.example.hangtimeassistant

import java.util.*

class Model {
    var reminders = emptyMap<Int , Reminder>()
    var contacts = emptyMap<Int , Contact>()
    var events = emptyMap<Int , Event>()
    var categories = emptyMap<Int , Category>()
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