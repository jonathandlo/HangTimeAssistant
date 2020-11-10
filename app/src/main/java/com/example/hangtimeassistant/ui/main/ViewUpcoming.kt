package com.example.hangtimeassistant.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.annimon.stream.function.ThrowableIntSupplier
import com.example.hangtimeassistant.*
import kotlinx.android.synthetic.main.fragment_upcoming.*
import kotlinx.android.synthetic.main.item_reminder.view.*
import kotlinx.android.synthetic.main.item_reminder_header.view.*
import java.text.DateFormat
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

/**
 * A placeholder fragment containing a simple view.
 */
class ViewUpcoming : Fragment() {
    public var needsUpdating = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_upcoming, container, false)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onStart() {
        super.onStart()

    }

    override fun onResume() {
        super.onResume()

        if (needsUpdating) listUpcoming(HangTimeDB.getDatabase(context!!), 14)
        needsUpdating = false
    }

    private fun listUpcoming(db: HangTimeDB, daysToShow: Int){
        val now = Instant.now().truncatedTo(ChronoUnit.DAYS)
        val sortedReminders = emptyList<Contact>().toMutableList()

        // find eligible reminders
        for (contact in db.contactDao().getAll()) {
            if (!contact.reminder) continue

            val dayDiff = Duration.between(now, Instant.ofEpochMilli(contact.reminderStartDate)).toDays()
            if (dayDiff > daysToShow) continue

            contact.metaNextReminder = Calendar.getInstance().apply {
                timeInMillis = contact.reminderStartDate
                this.add(when (contact.reminderCadenceUnit) {
                    "days" -> Calendar.DAY_OF_MONTH
                    "weeks" -> Calendar.WEEK_OF_YEAR
                    "months" -> Calendar.MONTH
                    "years" -> Calendar.YEAR
                    else -> Calendar.DAY_OF_MONTH
                }, contact.reminderCadence.toInt())

                if (contact.reminderDelay) this.add(when (contact.reminderDelayUnit) {
                    "days" -> Calendar.DAY_OF_MONTH
                    "weeks" -> Calendar.WEEK_OF_YEAR
                    "months" -> Calendar.MONTH
                    "years" -> Calendar.YEAR
                    else -> Calendar.DAY_OF_MONTH
                }, contact.reminderDelayAmount.toInt())
            }.toInstant()

            sortedReminders.add(contact)
        }

        // build sorted UI
        layout_remind_items.removeAllViews()
        sortedReminders.sortBy { it.metaNextReminder }

        var lastDueDateDiff = Duration.between(now, Instant.EPOCH).toDays()
        for (contact in sortedReminders) {
            val lastCheckedInDayDiff = Duration.between(now, Instant.ofEpochMilli(contact.reminderStartDate)).toDays()
            val dueDateDayDiff = Duration.between(now, contact.metaNextReminder).toDays()
            val entry = layoutInflater.inflate(R.layout.item_reminder, null)

            // create a section header if this is a new day
            if (lastDueDateDiff != dueDateDayDiff){
                val header = layoutInflater.inflate(R.layout.item_reminder_header, null)
                header.text_remind_header_name.text = DateFormat.getDateInstance(DateFormat.LONG).format(Date(contact.metaNextReminder.toEpochMilli()))
                header.text_remind_header_caption.text = when {
                    dueDateDayDiff < -1 -> "${-dueDateDayDiff} days ago"
                    dueDateDayDiff == -1L -> "1 day ago"
                    dueDateDayDiff == 0L -> "today"
                    dueDateDayDiff == 1L -> "in 1 day"
                    else -> "in $dueDateDayDiff days"
                }

                layout_remind_items.addView(header)
                lastDueDateDiff = dueDateDayDiff
            }

            // display details
            if (dueDateDayDiff < 0) {
                entry.text_remind_item_name.setTextColor(ContextCompat.getColor(context!!, R.color.colorAccent))
            }
            else if (dueDateDayDiff == 0L) {
                entry.text_remind_item_name.setTextColor(ContextCompat.getColor(context!!, R.color.colorPrimary))
            }

            entry.text_remind_item_name.text = contact.name

            entry.text_remind_item_caption.text = when {
                lastCheckedInDayDiff < 0L -> "last checked ${-lastCheckedInDayDiff} day${if (lastCheckedInDayDiff != -1L) "s ago" else " ago"}"
                lastCheckedInDayDiff == 0L -> "last checked today"
                else -> "first check in $lastCheckedInDayDiff day${if (lastCheckedInDayDiff != -1L) "s" else ""}"
            }

            // bind buttons
            entry.button_remind_item_check.setOnClickListener {
                contact.reminderStartDate = now.truncatedTo(ChronoUnit.DAYS).toEpochMilli()
                contact.reminderDelay = false
                db.contactDao().update(contact)
                ViewContacts.getInstance().needsUpdating = true

                listUpcoming(db, daysToShow)
            }

            layout_remind_items.addView(entry)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): ViewUpcoming {
            instance = ViewUpcoming().apply {
                arguments = Bundle().apply {

                }
            }

            return instance!!
        }

        private var instance: ViewUpcoming? = null
        fun getInstance(): ViewUpcoming {
            return instance ?: newInstance()
        }
    }
}