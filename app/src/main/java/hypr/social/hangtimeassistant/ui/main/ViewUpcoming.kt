package hypr.social.hangtimeassistant.ui.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.applandeo.materialcalendarview.DatePicker
import com.applandeo.materialcalendarview.listeners.OnSelectDateListener
import com.applandeo.materialcalendarview.utils.CalendarProperties
import hypr.social.hangtimeassistant.R
import hypr.social.hangtimeassistant.model.Contact
import hypr.social.hangtimeassistant.model.HTAFirestore
import kotlinx.android.synthetic.main.fragment_upcoming.*
import kotlinx.android.synthetic.main.item_reminder.view.*
import kotlinx.android.synthetic.main.item_reminder_edit.view.*
import kotlinx.android.synthetic.main.item_reminder_header.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

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

        if (needsUpdating) listUpcoming(14)
        needsUpdating = false
    }

    class ContactAndReminder(
        val contact: Contact,
        var metaNextReminder: Instant = Instant.EPOCH
    )

    private fun listUpcoming(daysToShow: Int){
        val now = Instant.now().truncatedTo(ChronoUnit.DAYS)
        val sortedReminders = emptyList<ContactAndReminder>().toMutableList()

        // find eligible, upcoming reminders
        lifecycleScope.launch(Main) {
            for (contact in withContext(IO) { HTAFirestore.getAllContacts() }) {
                if (!contact.reminder) continue

                var metaNextReminder = Instant.EPOCH
                val dayDiff = Duration.between(now, Instant.ofEpochMilli(contact.reminderStartDate)).toDays()

                if (dayDiff > daysToShow) continue

                metaNextReminder = Calendar.getInstance().apply {
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

                sortedReminders.add(ContactAndReminder(contact, metaNextReminder))
            }

            sortedReminders.sortBy { it.metaNextReminder }
            layout_remind_items.removeAllViews()

            // build sorted UI
            var lastDueDateDiff = Duration.between(now, Instant.EPOCH).toDays()
            for (contactAndReminder in sortedReminders) {
                val lastCheckedInDayDiff = Duration.between(now, Instant.ofEpochMilli(contactAndReminder.contact.reminderStartDate)).toDays()
                val dueDateDayDiff = Duration.between(now, contactAndReminder.metaNextReminder).toDays()
                val entry = layoutInflater.inflate(R.layout.item_reminder, null)

                // create a section header if this is a new day
                if (lastDueDateDiff != dueDateDayDiff) {
                    val header = layoutInflater.inflate(R.layout.item_reminder_header, null)
                    header.text_remind_header_name.text = DateFormat.getDateInstance(DateFormat.LONG).format(Date(contactAndReminder.metaNextReminder.toEpochMilli()))
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
                if (dueDateDayDiff < 0)
                    entry.text_remind_item_name.setTextColor(ContextCompat.getColor(context!!, R.color.colorAccent))
                else if (dueDateDayDiff == 0L)
                    entry.text_remind_item_name.setTextColor(ContextCompat.getColor(context!!, R.color.colorPrimary))

                entry.text_remind_item_name.text = contactAndReminder.contact.name

                entry.text_remind_item_caption.text = when {
                    lastCheckedInDayDiff < 0L -> "last checked ${-lastCheckedInDayDiff} day${if (lastCheckedInDayDiff != -1L) "s ago" else " ago"}"
                    lastCheckedInDayDiff == 0L -> "last checked today"
                    else -> "first check in $lastCheckedInDayDiff day${if (lastCheckedInDayDiff != -1L) "s" else ""}"
                }

                // bind buttons to refresh view again
                entry.button_remind_item_check.setOnClickListener {
                    contactAndReminder.contact.reminderStartDate = now.truncatedTo(ChronoUnit.DAYS).toEpochMilli()
                    contactAndReminder.contact.reminderDelay = false

                    lifecycleScope.launch(IO) {
                        HTAFirestore.update(contactAndReminder.contact)
                        ViewContacts.getInstance().needsUpdating = true
                    }

                    listUpcoming(daysToShow)
                }

                createDialog(entry, contactAndReminder.contact)

                layout_remind_items.addView(entry)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private suspend fun createDialog(entry: View, contact: Contact) {
        withContext(Main) {
            // create edit dialog
            val editButton = entry.button_remind_item_edit
            editButton.setOnClickListener {
                CoroutineScope(Main).launch {
                    val dialogView = createReminderEdit(contact)
                    val alertDialog = AlertDialog.Builder(context!!, R.style.Theme_MaterialComponents_Light_Dialog_Alert)
                        .setView(dialogView.apply {
                            // close keyboard on dialog touch
                            setOnTouchListener { v, event ->
                                if (v != null) {
                                    val imm = this@ViewUpcoming.activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                                }
                                true
                            }
                        })
                        .setPositiveButton("Close") { dialogInterface: DialogInterface, i: Int -> }
                        .setOnDismissListener {
                            if (needsUpdating) listUpcoming(14)
                            needsUpdating = false
                        }
                        .create()

                    alertDialog.show()
                }
            }
        }
    }

    private suspend fun createReminderEdit(contact: Contact) : View {
        return withContext(Main) {
            // one first click, create the collapsible view
            val reminderView = layoutInflater.inflate(R.layout.item_reminder_edit, null)
            reminderView.id = View.generateViewId()

            // inflate remaining controls
            reminderView.cb_cont_reminder.visibility = View.VISIBLE

            // display reminder controls only if reminder checked
            reminderView.table_rem_details.alpha = 0f
            reminderView.table_rem_details.visibility = View.GONE
            reminderView.cb_cont_reminder.setOnCheckedChangeListener { buttonView, isChecked ->
                contact.reminder = isChecked
                lifecycleScope.launch(IO) {
                    HTAFirestore.update(contact)

                    this@ViewUpcoming.needsUpdating = true
                    ViewContacts.getInstance().needsUpdating = true
                }

                if (isChecked) {
                    reminderView.table_rem_details.visibility = View.VISIBLE
                    reminderView.table_rem_details.animate().alpha(1f)
                } else {
                    reminderView.table_rem_details.animate()
                        .alpha(0f)
                        .withEndAction {
                            reminderView.table_rem_details.visibility = View.GONE
                        }
                }
            }

            // update reminder details
            reminderView.cb_cont_reminder.isChecked = contact.reminder
            reminderView.numpick_cont_reminder.setText(contact.reminderCadence.toString())
            reminderView.spinner_reminder.setSelection(
                when (contact.reminderCadenceUnit) {
                    "days" -> 0
                    "weeks" -> 1
                    "months" -> 2
                    "years" -> 3
                    else -> 0
                }, true
            )
            reminderView.text_cont_startdate.text = DateFormat.getDateInstance().format(Date(contact.reminderStartDate))
            reminderView.cb_cont_delay.isChecked = contact.reminderDelay
            reminderView.numpick_cont_delay.setText(contact.reminderDelayAmount.toString())
            reminderView.spinner_delay.setSelection(
                when (contact.reminderDelayUnit) {
                    "days" -> 0
                    "weeks" -> 1
                    "months" -> 2
                    "years" -> 3
                    else -> 0
                }, true
            )

            // add value changed listeners
            reminderView.numpick_cont_reminder.addTextChangedListener {
                contact.reminderCadence = it.toString().toLongOrNull() ?: 7

                lifecycleScope.launch(IO) {
                    HTAFirestore.update(contact)

                    this@ViewUpcoming.needsUpdating = true
                    ViewContacts.getInstance().needsUpdating = true
                }
            }
            reminderView.spinner_reminder.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    contact.reminderCadenceUnit = when (position) {
                        0 -> "days"
                        1 -> "weeks"
                        2 -> "months"
                        3 -> "years"
                        else -> "days"
                    }

                    lifecycleScope.launch(IO) {
                        HTAFirestore.update(contact)

                        this@ViewUpcoming.needsUpdating = true
                        ViewContacts.getInstance().needsUpdating = true
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    contact.reminderCadenceUnit = "days"

                    lifecycleScope.launch(IO) {
                        HTAFirestore.update(contact)

                        this@ViewUpcoming.needsUpdating = true
                        ViewContacts.getInstance().needsUpdating = true
                    }
                }
            }
            reminderView.btn_cont_datepick.setOnClickListener {
                DatePicker(reminderView.context!!, CalendarProperties(reminderView.context!!).apply {
                    this.todayColor = ContextCompat.getColor(context!!, R.color.colorPrimary)
                    this.selectionColor = ContextCompat.getColor(context!!, R.color.colorAccent)
                    this.setSelectedDay(Calendar.getInstance().apply { this.timeInMillis = contact.reminderStartDate })
                    this.onSelectDateListener = object : OnSelectDateListener {
                        override fun onSelect(calendar: List<Calendar>) {
                            contact.reminderStartDate = calendar[0].time.toInstant().truncatedTo(ChronoUnit.DAYS).toEpochMilli()
                            reminderView.text_cont_startdate.text = DateFormat.getDateInstance().format(Date(contact.reminderStartDate))

                            lifecycleScope.launch(IO) {
                                HTAFirestore.update(contact)
                                this@ViewUpcoming.needsUpdating = true
                                ViewContacts.getInstance().needsUpdating = true
                            }
                        }
                    }
                }).show()
            }
            reminderView.cb_cont_delay.setOnCheckedChangeListener { buttonView, isChecked ->
                contact.reminderDelay = isChecked

                lifecycleScope.launch(IO) {
                    HTAFirestore.update(contact)

                    this@ViewUpcoming.needsUpdating = true
                    ViewContacts.getInstance().needsUpdating = true
                }
            }
            reminderView.numpick_cont_delay.addTextChangedListener {
                contact.reminderDelayAmount = it.toString().toLongOrNull() ?: 0
                lifecycleScope.launch(IO) {
                    HTAFirestore.update(contact)

                    this@ViewUpcoming.needsUpdating = true
                    ViewContacts.getInstance().needsUpdating = true
                }
            }
            reminderView.spinner_delay.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    contact.reminderDelayUnit = when (position) {
                        0 -> "days"
                        1 -> "weeks"
                        2 -> "months"
                        3 -> "years"
                        else -> "days"
                    }

                    lifecycleScope.launch(IO) {
                        HTAFirestore.update(contact)

                        this@ViewUpcoming.needsUpdating = true
                        ViewContacts.getInstance().needsUpdating = true
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    contact.reminderDelayUnit = "days"

                    lifecycleScope.launch(IO) {
                        HTAFirestore.update(contact)

                        this@ViewUpcoming.needsUpdating = true
                        ViewContacts.getInstance().needsUpdating = true
                    }
                }
            }

            return@withContext reminderView
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