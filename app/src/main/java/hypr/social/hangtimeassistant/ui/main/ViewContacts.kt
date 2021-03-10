package hypr.social.hangtimeassistant.ui.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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
import androidx.lifecycle.lifecycleScope
import com.applandeo.materialcalendarview.DatePicker
import com.applandeo.materialcalendarview.listeners.OnSelectDateListener
import com.applandeo.materialcalendarview.utils.CalendarProperties
import com.birjuvachhani.locus.Locus
import hypr.social.hangtimeassistant.R
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import hypr.social.hangtimeassistant.model.Contact
import hypr.social.hangtimeassistant.model.HTAFirestore
import kotlinx.android.synthetic.main.fragment_contact.*
import kotlinx.android.synthetic.main.item_contact.view.*
import kotlinx.android.synthetic.main.item_contact_collapsible_edit.view.*
import kotlinx.android.synthetic.main.item_contact_collapsible_edit.view.flexbox_categories
import kotlinx.android.synthetic.main.item_contact_collapsible_viewonly.view.*
import kotlinx.android.synthetic.main.item_reminder_edit.view.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import java.text.DateFormat
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

class ViewContacts : Fragment() {
    public var needsUpdating = true

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
        
        // configure the search box
        textinput_cont_search.doOnTextChanged { text, start, before, count ->
                numSearches++
                val searchNumber = numSearches
                val thisQuery = text.toString().trim()

                // leave if these results are already being displayed
                if (thisQuery == displayedQuery) return@doOnTextChanged
                println("new search query: $thisQuery")

                lifecycleScope.launch(IO) {
                    delay(500)

                    // leave if this is not the latest search
                    if (searchNumber != numSearches) return@launch
                    println("launching search: $thisQuery")

                    // trigger a new search
                    withContext(Main) {
                        listContacts(thisQuery)
                        displayedQuery = thisQuery
                    }
                }
        }

        // configure the add contact button
        button_cont_add.setOnClickListener {
            lifecycleScope.launch(IO) {
                val contactView = addItem(
                    HTAFirestore.add(Contact().apply {
                        this.reminderStartDate = Instant.now().truncatedTo(ChronoUnit.DAYS).toEpochMilli()
                    })
                )
                withContext(Main) {
                    layout_cont_items.addView(contactView)
                    contactView!!.layout_cont_collapsible_view.button_cont_edit_view.performClick()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

    }

    override fun onResume() {
        super.onResume()

        if (needsUpdating) listContacts()
        needsUpdating = false
    }

    private fun listContacts(searchTerm: String = ""){
        layout_cont_items.removeAllViews()

        // populate the view with contacts
        lifecycleScope.launch(IO) {
            for (contact in HTAFirestore.getAllContacts()) {
                if (searchTerm.isNotEmpty()
                    && !contact.name.contains(searchTerm, true)
                    && !contact.address.contains(searchTerm, true)
                    && !contact.phoneNum.contains(searchTerm, true)
                    && !contact.FBUrl.contains(searchTerm, true)
                    && !contact.IGUrl.contains(searchTerm, true)
                ) continue
                
                withContext(Main) {
                    val contactView = addItem(contact)
                    layout_cont_items.addView(contactView)
                }
            }
        }
    }

    private suspend fun addItem(contact: Contact): View? {
        // inflate the contact xml
        return withContext(Main) {
            val contactView = layoutInflater.inflate(R.layout.item_contact, null)

            createContactView(contactView, contact)
            updateContactView(contactView, contact)

            return@withContext contactView
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createContactView(contactView: View, contact: Contact): View {
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

                    lifecycleScope.launch(IO) {
                        HTAFirestore.unlink(contact)
                        HTAFirestore.delete(contact)

                        ViewUpcoming.getInstance().needsUpdating = true
                    }

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
            lifecycleScope.launch(Main) {
                val dialogView = createContactEdit(contact)
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
                        lifecycleScope.launch(Main) { updateContactView(contactView, contact) }
                    }
                    .create()

                alertDialog.show()

                // configure edit dialog's delete button
                dialogView.button_cont_delete.setOnClickListener {
                    AlertDialog.Builder(context!!)
                        .setTitle("Delete contact?")
                        .setPositiveButton("OK") { dialogInterface: DialogInterface, i: Int ->
                            it.isClickable = false

                            lifecycleScope.launch(IO) {
                                HTAFirestore.unlink(contact)
                                HTAFirestore.delete(contact)

                                ViewUpcoming.getInstance().needsUpdating = true
                            }

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

    private suspend fun updateContactView(contactView: View, contact: Contact){
        val collapsible = contactView.layout_cont_collapsible_view

        // update details
        contactView.text_cont_name.text = contact.name
        contactView.img_cont_map_ind.visibility = if(contact.address.trim().isNotEmpty()) View.VISIBLE else View.GONE
        collapsible.text_phone_view.text = contact.phoneNum
        collapsible.text_address_view.text = contact.address
        collapsible.text_fb_view.text = contact.FBUrl
        collapsible.text_ig_view.text = contact.IGUrl

        // show reminder details
        if (!contact.reminder) collapsible.text_cont_reminder.text = "No reminders"
        else {
            collapsible.text_cont_reminder.text = """Every ${contact.reminderCadence} ${contact.reminderCadenceUnit}${System.lineSeparator()}""" +
            """Starting on ${DateFormat.getDateInstance().format(Date(contact.reminderStartDate))}""" +
            if (contact.reminderDelay) """${System.lineSeparator()}Delayed for ${contact.reminderDelayAmount} ${contact.reminderDelayUnit}"""
            else ""
        }

        // show categories
        collapsible.flexbox_categories.removeAllViews()

        val linkedCategories = withContext(IO){ HTAFirestore.getCategories(contact) }
        for (category in linkedCategories) {
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

    private suspend fun createContactEdit(contact: Contact) : View {
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
                    contact.name = s.toString()

                    lifecycleScope.launch(IO) {
                        HTAFirestore.update(contact)

                        ViewUpcoming.getInstance().needsUpdating = true
                        ViewCategories.getInstance().needsUpdating = true
                        ViewEvents.getInstance().needsUpdating = true
                        ViewMap.getInstance().needsUpdating = true
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        collapsible.text_phone.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (collapsible.text_phone.hasFocus()) {
                    contact.phoneNum = s.toString()
                    
                    lifecycleScope.launch(IO) {
                        HTAFirestore.update(contact)
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        collapsible.text_address.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (collapsible.text_address.hasFocus()) {
                    contact.address = s.toString()

                    lifecycleScope.launch(IO) {
                        HTAFirestore.update(contact)
                        ViewMap.getInstance().needsUpdating = true
                    }

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
                    contact.FBUrl = s.toString()

                    lifecycleScope.launch(IO) {
                        HTAFirestore.update(contact)
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        collapsible.text_ig.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (collapsible.text_ig.hasFocus()) {
                    contact.IGUrl = s.toString()
                    
                    lifecycleScope.launch(IO) {
                        HTAFirestore.update(contact)
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        createReminderEdit(contact, collapsible)

        // show categories
        collapsible.flexbox_categories.removeAllViews()

        withContext(IO) {
            for (category in HTAFirestore.getAllCategories()) {
                val linked = HTAFirestore.linked(contact, category)

                withContext(Main) {
                    // style the category card
                    val categoryCard = Button(context)
                    categoryCard.id = View.generateViewId()

                    categoryCard.minimumWidth = 0
                    categoryCard.minWidth = 0
                    categoryCard.minimumHeight = 0
                    categoryCard.minHeight = 0
                    categoryCard.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12f)
                    categoryCard.text = category.name

                    // darken unassociated categories
                    var showColor = category.color

                    if (linked)
                        categoryCard.setTextColor(Color.argb(110, 0, 0, 0))
                    else{
                        showColor = Color.rgb(
                            Color.red(showColor) / 4 + 50,
                            Color.green(showColor) / 4 + 50,
                            Color.blue(showColor) / 4 + 50
                        )
                        categoryCard.setTextColor(Color.argb(55, 255, 255, 255))
                    }

                    DrawableCompat.setTint(DrawableCompat.wrap(categoryCard.background).mutate(), showColor)

                    // add click handlers
                    categoryCard.setOnClickListener {
                        lifecycleScope.launch(IO) {
                            if (HTAFirestore.linked(contact, category)) {
                                // remove association
                                HTAFirestore.unlink(contact, category)
                                ViewCategories.getInstance().needsUpdating = true

                                withContext(Main) {
                                    val backColor = Color.rgb(
                                        Color.red(category.color) / 4 + 50,
                                        Color.green(category.color) / 4 + 50,
                                        Color.blue(category.color) / 4 + 50
                                    )
                                    categoryCard.setTextColor(Color.argb(55, 255, 255, 255))
                                    DrawableCompat.setTint(DrawableCompat.wrap(categoryCard.background).mutate(), backColor)
                                }
                            } else {
                                // add association
                                HTAFirestore.link(contact, category)
                                ViewCategories.getInstance().needsUpdating = true

                                withContext(Main) {
                                    DrawableCompat.setTint(DrawableCompat.wrap(categoryCard.background).mutate(), category.color)
                                    categoryCard.setTextColor(Color.argb(110, 0, 0, 0))
                                }
                            }
                        }
                    }

                    collapsible.flexbox_categories.addView(categoryCard)
                }
            }
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

            lifecycleScope.launch(IO) {
                HTAFirestore.update(contact)
                ViewUpcoming.getInstance().needsUpdating = true
            }

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
            contact.reminderCadence = it.toString().toLongOrNull()?: 7

            lifecycleScope.launch(IO) {
                HTAFirestore.update(contact)
                ViewUpcoming.getInstance().needsUpdating = true
            }
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

                lifecycleScope.launch(IO) {
                    HTAFirestore.update(contact)
                    ViewUpcoming.getInstance().needsUpdating = true
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                contact.reminderCadenceUnit = "days"

                lifecycleScope.launch(IO) {
                    HTAFirestore.update(contact)
                    ViewUpcoming.getInstance().needsUpdating = true
                }
            }
        }
        pParentView.btn_cont_datepick.setOnClickListener {
            DatePicker(pParentView.context!!, CalendarProperties(pParentView.context!!).apply {
                this.todayColor = ContextCompat.getColor(context!!, R.color.colorPrimary)
                this.selectionColor = ContextCompat.getColor(context!!, R.color.colorAccent)
                this.setSelectedDay(Calendar.getInstance().apply { this.timeInMillis = contact.reminderStartDate })
                this.onSelectDateListener = object : OnSelectDateListener {
                    override fun onSelect(calendar: List<Calendar>) {
                        contact.reminderStartDate = calendar[0].time.toInstant().truncatedTo(ChronoUnit.DAYS).toEpochMilli()
                        pParentView.text_cont_startdate.text = DateFormat.getDateInstance().format(Date(contact.reminderStartDate))

                        lifecycleScope.launch(IO) {
                            HTAFirestore.update(contact)
                            ViewUpcoming.getInstance().needsUpdating = true
                        }
                    }
                }
            }).show()
        }
        pParentView.cb_cont_delay.setOnCheckedChangeListener { buttonView, isChecked ->
            contact.reminderDelay = isChecked

            lifecycleScope.launch(IO) {
                HTAFirestore.update(contact)
                ViewUpcoming.getInstance().needsUpdating = true
            }
        }
        pParentView.numpick_cont_delay.addTextChangedListener {
            contact.reminderDelayAmount = it.toString().toLongOrNull()?: 0

            lifecycleScope.launch(IO) {
                HTAFirestore.update(contact)
                ViewUpcoming.getInstance().needsUpdating = true
            }
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

                lifecycleScope.launch(IO) {
                    HTAFirestore.update(contact)
                    ViewUpcoming.getInstance().needsUpdating = true
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                contact.reminderDelayUnit = "days"

                lifecycleScope.launch(IO) {
                    HTAFirestore.update(contact)
                    ViewUpcoming.getInstance().needsUpdating = true
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): ViewContacts {
            instance = ViewContacts().apply {
                arguments = Bundle().apply {

                }
            }

            return instance!!
        }

        private var instance: ViewContacts? = null
        fun getInstance(): ViewContacts {
            return instance ?: newInstance()
        }
    }
}