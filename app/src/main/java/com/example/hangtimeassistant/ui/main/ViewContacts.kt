package com.example.hangtimeassistant.ui.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import com.applandeo.materialcalendarview.DatePicker
import com.applandeo.materialcalendarview.listeners.OnSelectDateListener
import com.applandeo.materialcalendarview.utils.CalendarProperties
import com.applandeo.materialcalendarview.utils.SelectedDay
import com.birjuvachhani.locus.Locus
import com.example.hangtimeassistant.Contact
import com.example.hangtimeassistant.HangTimeDB
import com.example.hangtimeassistant.R
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import kotlinx.android.synthetic.main.fragment_contact.*
import kotlinx.android.synthetic.main.item_contact.view.*
import kotlinx.android.synthetic.main.item_contact_collapsible_edit.view.*
import kotlinx.android.synthetic.main.item_contact_collapsible_edit.view.flexbox_categories
import kotlinx.android.synthetic.main.item_contact_collapsible_viewonly.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*


/**
 * A placeholder fragment containing a simple view.
 */
class ViewContacts : Fragment() {
    var numSearches = 0
    var displayedQuery = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_contact, container, false)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // init places api for autocomplete
        if (!Places.isInitialized()) Places.initialize(context!!, getString(R.string.maps_api_key))
        Places.createClient(context!!)

        listContacts()
        
        // configure the search box
        textinput_cont_search.doOnTextChanged { text, start, before, count ->
                numSearches++
                val searchNumber = numSearches
                val thisQuery = text.toString().trim()

                // leave if these results are already being displayed
                if (thisQuery == displayedQuery) return@doOnTextChanged
                println("new search query: $thisQuery")

                GlobalScope.launch {
                    delay(1000)

                    // leave if this is not the latest search
                    if (searchNumber != numSearches) return@launch
                    println("launching search: $thisQuery")

                    // trigger a new search
                    activity!!.runOnUiThread {
                        listContacts(thisQuery)
                        displayedQuery = thisQuery
                    }
                }
        }

        // configure the add contact button
        button_cont_add.setOnClickListener {
            val db = HangTimeDB.getDatabase(this.context!!)
            val contactView = addItem(db.contactDao().getRow(db.contactDao().insert(Contact().apply { this.reminderStartDate = Calendar.getInstance().timeInMillis })), db)
            layout_cont_items.addView(contactView)
            contactView!!.layout_cont_collapsible_view.button_cont_edit_view.performClick()
        }
    }

    private fun listContacts(searchTerm: String = ""){
        val db = HangTimeDB.getDatabase(context!!)
        layout_cont_items.removeAllViews()

        // populate the view with contacts
        for (contact in db.contactDao().getAll()){
            if (searchTerm.isNotEmpty()
                && !contact.name.contains(searchTerm, true)
                && !contact.address.contains(searchTerm, true)
                && !contact.phoneNum.contains(searchTerm, true)
                && !contact.FBUrl.contains(searchTerm, true)
                && !contact.IGUrl.contains(searchTerm, true)) continue

            val contactView = addItem(contact, db)
            layout_cont_items.addView(contactView)
        }
    }

    private fun addItem(contact: Contact, db: HangTimeDB): View? {
        // inflate the contact xml
        val contactView = layoutInflater.inflate(R.layout.item_contact, null)

        createContactView(contactView, contact, db)
        updateContactView(contactView, contact, db)

        return contactView
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createContactView(contactView: View, contact: Contact, db: HangTimeDB): View {
        // create collapsible layout
        val collapsible = layoutInflater.inflate(R.layout.item_contact_collapsible_viewonly, null)
        collapsible.visibility = View.GONE
        collapsible.alpha = 0f

        // create delete button
        val delButton = collapsible.button_cont_delete_view
        delButton.id = View.generateViewId()
        delButton.setOnClickListener {
            AlertDialog.Builder(context!!)
                .setTitle("Delete contact?")
                .setPositiveButton("OK") { dialogInterface: DialogInterface, i: Int ->
                    it.isClickable = false
                    db.contactDao().delete(contact)
                    db.contactDao().deleteAssociations(contact.ID)
                    contactView.animate()
                        .alpha(0f)
                        .withEndAction {
                            contactView.visibility = View.GONE
                            layout_cont_items.removeView(contactView)
                        }
                }
                .setNegativeButton("Cancel") { dialogInterface: DialogInterface, i: Int -> }
                .create()
                .show()
        }

        // create edit dialog
        val editButton = collapsible.button_cont_edit_view
        editButton.setOnClickListener {
            val dialogView = createContactEdit(contact, db)
            val alertDialog = AlertDialog.Builder(context!!, R.style.Theme_MaterialComponents_Light_Dialog_Alert)
                .setView(dialogView.apply {
                    // close keyboard on dialog touch
                    setOnTouchListener { v, event ->
                        if (v != null) {
                            val imm = this@ViewContacts.activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.hideSoftInputFromWindow(v.windowToken, 0)
                        }
                        true
                    }
                    text_cont_name_edit.requestFocus()

                    // open the soft keyboard for new contacts
                    if (contact.name.isBlank()) {
                        text_cont_name_edit.postDelayed(
                            {
                                val imm = this@ViewContacts.activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                imm.showSoftInput(text_cont_name_edit, 0)
                            }, 200
                        )
                    }
                })
                .setPositiveButton("Close") { dialogInterface: DialogInterface, i: Int -> }
                .setOnDismissListener {
                    smoothScrollToTop(contactView)
                            updateContactView(contactView, contact, db)
                }
                .create()

            alertDialog.show()

            // configure edit dialog's delete button
            dialogView.button_cont_delete.setOnClickListener {
                AlertDialog.Builder(context!!)
                    .setTitle("Delete contact?")
                    .setPositiveButton("OK") { dialogInterface: DialogInterface, i: Int ->
                        it.isClickable = false
                        db.contactDao().delete(contact)
                        db.contactDao().deleteAssociations(contact.ID)
                        alertDialog.dismiss()
                        contactView.animate()
                            .alpha(0f)
                            .withEndAction {
                                contactView.visibility = View.GONE
                                layout_cont_items.removeView(contactView)
                            }
                    }
                    .setNegativeButton("Cancel") { dialogInterface: DialogInterface, i: Int -> }
                    .create()
                    .show()
            }
        }

        contactView.layout_cont_item_main.addView(collapsible)

        // create show/hide chevron
        contactView.layout_cont_title_area.setOnClickListener {
            if (collapsible.visibility == View.VISIBLE) {
                // if visible, hide the view
                collapsible.animate()
                    .alpha(0f)
                    .withEndAction {
                        collapsible.visibility = View.GONE
                    }

                it.img_cont_chevron.animate().rotation(0f)
            } else {
                // if hidden, show the view
                collapsible.visibility = View.VISIBLE
                collapsible.animate()
                    .alpha(1f)
                    .setStartDelay(100)
                    .withStartAction {
                        smoothScrollToTop(contactView)
                    }

                it.img_cont_chevron.animate().rotation(180f)
            }
        }

        return collapsible
    }

    private fun smoothScrollToTop(contactView: View) {
        val rect = Rect()
        scrollview_cont.offsetDescendantRectToMyCoords(contactView, rect)
        scrollview_cont.smoothScrollTo(0, rect.top)
    }

    private fun updateContactView(contactView: View, contact: Contact, db: HangTimeDB){
        val collapsible = contactView.layout_cont_collapsible_view

        // update details
        contactView.text_cont_name.text = contact.name
        collapsible.text_phone_view.text = contact.phoneNum
        collapsible.text_address_view.text = contact.address
        collapsible.text_fb_view.text = contact.FBUrl
        collapsible.text_ig_view.text = contact.IGUrl

        // show categories
        collapsible.flexbox_categories.removeAllViews()

        for (category in db.contactDao().loadCategories(contact.ID)) {
            collapsible.flexbox_categories.addView(Button(context).apply {
                id = View.generateViewId()

                minimumWidth = 0
                minWidth = 0
                minimumHeight = 0
                minHeight = 0
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12f)
                text = category.name

                // darken unassociated categories
                setTextColor(Color.argb(110, 0, 0, 0))
                DrawableCompat.setTint(DrawableCompat.wrap(background).mutate(), category.color)
            })
        }
    }

    var lastLocation : LatLng? = null

    private fun createContactEdit(contact: Contact, db: HangTimeDB) : View {
        // one first click, create the collapsible view
        val collapsible = layoutInflater.inflate(R.layout.item_contact_collapsible_edit, null)
        collapsible.id = View.generateViewId()

        // update details
        collapsible.text_cont_name_edit.setText(contact.name)
        collapsible.text_phone.setText(contact.phoneNum)
        collapsible.text_address.setText(contact.address)
        collapsible.text_fb.setText(contact.FBUrl)
        collapsible.text_ig.setText(contact.IGUrl)

        // add text changed listeners
        collapsible.text_cont_name_edit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (collapsible.text_cont_name_edit.hasFocus()) {
                    db.contactDao().update(contact.apply { name = s.toString() })
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        collapsible.text_phone.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (collapsible.text_phone.hasFocus()) {
                    db.contactDao().update(contact.apply { phoneNum = s.toString() })
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        collapsible.text_address.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (collapsible.text_address.hasFocus()) {
                    db.contactDao().update(contact.apply { address = s.toString() })

                    // update last known location async
                    Locus.getCurrentLocation(context!!) { locusResult ->
                        locusResult.location?.let {
                            lastLocation = LatLng(it.latitude, it.longitude)
                        }
                    }

                    // request autocomplete places list
                    val request = FindAutocompletePredictionsRequest.builder()
                            .setOrigin(lastLocation)
                            .setSessionToken(AutocompleteSessionToken.newInstance())
                            .setQuery(s.toString())
                            .build()
                    Places.createClient(context!!).findAutocompletePredictions(request)
                        .addOnSuccessListener { response: FindAutocompletePredictionsResponse ->
                            // get array of results
                            val resultArr = response.autocompletePredictions.map { it.getFullText(null).toString() }

                            // apply list of results
                            val adapter: ArrayAdapter<String> = ArrayAdapter<String>(context!!, android.R.layout.simple_dropdown_item_1line, resultArr)
                            collapsible.text_address.setAdapter(adapter)
                        }
                        .addOnFailureListener { exception: Exception? ->
                            if (exception is ApiException) {
                                // no places found
                            }
                        }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        collapsible.text_fb.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (collapsible.text_fb.hasFocus()) {
                    db.contactDao().update(contact.apply { FBUrl = s.toString() })
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        collapsible.text_ig.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (collapsible.text_ig.hasFocus()) {
                    db.contactDao().update(contact.apply { IGUrl = s.toString() })
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        createReminderEdit(contact, collapsible)

        // show categories
        collapsible.flexbox_categories.removeAllViews()

        for (category in db.categoryDao().getAll()) {
            collapsible.flexbox_categories.addView(Button(context).apply {
                id = View.generateViewId()

                minimumWidth = 0
                minWidth = 0
                minimumHeight = 0
                minHeight = 0
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12f)
                text = category.name

                // darken unassociated categories
                var showColor = category.color
                if (db.contactDao().countCategories(contact.ID, category.ID) == 0) {
                    showColor = Color.rgb(
                        Color.red(showColor) / 4 + 50,
                        Color.green(showColor) / 4 + 50,
                        Color.blue(showColor) / 4 + 50
                    )
                    setTextColor(Color.argb(55, 255, 255, 255))
                } else setTextColor(Color.argb(110, 0, 0, 0))

                DrawableCompat.setTint(DrawableCompat.wrap(background).mutate(), showColor)

                // add click handlers
                setOnClickListener {
                    if (db.contactDao().countCategories(contact.ID, category.ID) > 0) {
                        // remove association
                        db.contactDao().unlinkCategory(contact.ID, category.ID)

                        val backColor = Color.rgb(
                            Color.red(category.color) / 4 + 50,
                            Color.green(category.color) / 4 + 50,
                            Color.blue(category.color) / 4 + 50
                        )
                        setTextColor(Color.argb(55, 255, 255, 255))
                        DrawableCompat.setTint(DrawableCompat.wrap(background).mutate(), backColor)
                    } else {
                        // add association
                        db.contactDao().linkCategory(contact.ID, category.ID)
                        DrawableCompat.setTint(DrawableCompat.wrap(background).mutate(), category.color)
                        setTextColor(Color.argb(110, 0, 0, 0))
                    }

                }
            })
        }

        return collapsible
    }

    private fun createReminderEdit(contact: Contact, pParentView: View) {
        // inflate remaining controls
        pParentView.cb_cont_reminder.visibility = View.VISIBLE

        // display reminder controls only if reminder checked
        pParentView.table_rem_details.alpha = 0f
        pParentView.table_rem_details.visibility = View.GONE
        pParentView.cb_cont_reminder.setOnCheckedChangeListener { buttonView, isChecked ->
            contact.reminder = isChecked

            if (isChecked){
                pParentView.table_rem_details.visibility = View.VISIBLE
                pParentView.table_rem_details.animate().alpha(1f)
            }
            else {
                pParentView.table_rem_details.animate()
                    .alpha(0f)
                    .withEndAction {
                        pParentView.table_rem_details.visibility = View.GONE
                    }
            }
        }

        // update reminder details
        pParentView.cb_cont_reminder.isChecked = contact.reminder
        pParentView.numpick_cont_reminder.setText(contact.reminderCadence.toString())
        pParentView.spinner_reminder.setSelection(
            when (contact.reminderCadenceUnit) {
                "days" -> 0
                "weeks" -> 1
                "months" -> 2
                "years" -> 3
                else -> 0
            }, true)
        pParentView.text_cont_startdate.text = DateFormat.getDateInstance().format(Date(contact.reminderStartDate))
        pParentView.cb_cont_delay.isChecked = contact.reminderDelay
        pParentView.numpick_cont_delay.setText(contact.reminderDelayAmount.toString())
        pParentView.spinner_delay.setSelection(
            when (contact.reminderDelayUnit) {
                "days" -> 0
                "weeks" -> 1
                "months" -> 2
                "years" -> 3
                else -> 0
            }, true)

        // add value changed listeners
        pParentView.numpick_cont_reminder.addTextChangedListener {
            contact.reminderCadence = it.toString().toLong()
        }
        pParentView.spinner_reminder.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                contact.reminderCadenceUnit = when (position){
                    0 -> "days"
                    1 -> "weeks"
                    2 -> "months"
                    3 -> "years"
                    else -> "days"
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                contact.reminderCadenceUnit = "days"
            }
        }
        pParentView.btn_cont_datepick.setOnClickListener {
            DatePicker(pParentView.context!!, CalendarProperties(pParentView.context!!).apply {
                this.todayColor = ContextCompat.getColor(context!!, R.color.colorPrimary)
                this.selectionColor = ContextCompat.getColor(context!!, R.color.colorAccent)
                this.setSelectedDay(Calendar.getInstance().apply { this.timeInMillis = contact.reminderStartDate })
                this.onSelectDateListener = object : OnSelectDateListener {
                    override fun onSelect(calendar: List<Calendar>) {
                        contact.reminderStartDate = calendar[0].time.time
                        pParentView.text_cont_startdate.text = DateFormat.getDateInstance().format(Date(contact.reminderStartDate))
                    }
                }
            }).show()
        }
        pParentView.cb_cont_delay.setOnCheckedChangeListener { buttonView, isChecked ->
            contact.reminderDelay = isChecked
        }
        pParentView.numpick_cont_delay.addTextChangedListener {
            contact.reminderDelayAmount = it.toString().toLong()
        }
        pParentView.spinner_delay.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                contact.reminderDelayUnit = when (position){
                    0 -> "days"
                    1 -> "weeks"
                    2 -> "months"
                    3 -> "years"
                    else -> "days"
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                contact.reminderCadenceUnit = "days"
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): ViewContacts {
            return ViewContacts().apply {
                arguments = Bundle().apply {

                }
            }
        }
    }
}