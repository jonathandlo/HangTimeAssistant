package com.example.hangtimeassistant.ui.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import com.example.hangtimeassistant.*
import kotlinx.android.synthetic.main.fragment_contact.*
import kotlinx.android.synthetic.main.fragment_events.*
import kotlinx.android.synthetic.main.fragment_events.textinput_cont_search
import kotlinx.android.synthetic.main.item_event.view.*
import kotlinx.android.synthetic.main.item_event_detail.*
import kotlinx.android.synthetic.main.item_event_detail.view.*
import kotlinx.android.synthetic.main.item_event_detail_guest.*
import kotlinx.android.synthetic.main.item_event_detail_guest.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * A placeholder fragment containing a simple view.
 */
class ViewEvents : Fragment() {
    public var needsUpdating = true
    var numSearches = 0
    var displayedQuery = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_events, container, false)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

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
                    listEvents(thisQuery)
                    displayedQuery = thisQuery
                }
            }
        }

        // configure the add event button
        button_event_add.setOnClickListener {
            val db = HangTimeDB.getDatabase(this.context!!)
            val eventView = addItem(db.eventDao().getRow(db.eventDao().insert(Event())), db)
            eventView.button_event_detail.performClick()
        }
    }

    override fun onStart() {
        super.onStart()

    }

    override fun onResume() {
        super.onResume()

        if (needsUpdating) listEvents()
        needsUpdating = false
    }

    private fun listEvents(searchTerm: String = ""){
        // populate the view with events
        val db = HangTimeDB.getDatabase(this.context!!)
        layout_event_items.removeAllViews()

        for (event in db.eventDao().getAll()){
            if (searchTerm.isNotEmpty()
                && !event.name.contains(searchTerm, true)
                && !event.address.contains(searchTerm, true)
                && !event.description.contains(searchTerm, true)) continue

            addItem(event, db)
        }
    }

    private fun addItem(event: Event, db: HangTimeDB) : View {
        val newEventItem = layoutInflater.inflate(R.layout.item_event, null)
        newEventItem.id = View.generateViewId()

        // configure name textbox
        val textName = newEventItem.text_event_name
        DrawableCompat.setTint(DrawableCompat.wrap(textName.background).mutate(), Color.TRANSPARENT)
        textName.id = View.generateViewId()
        textName.text = event.name

        // configure list button/dialog
        val listButton = newEventItem.button_event_detail
        listButton.setOnClickListener {
            val dialogView = createEventListDialog(event, db)
            val alertDialog = AlertDialog.Builder(context!!, R.style.Theme_MaterialComponents_Light_Dialog_Alert)
                .setView(dialogView)
                .setPositiveButton("Close") { dialogInterface: DialogInterface, i: Int -> }
                .setOnDismissListener {
                    event.name = dialogView.text_event_name_edit.text.toString()
                    db.eventDao().update(event)
                    textName.text = event.name
                }
                .create()

            // configure delete button
            dialogView.button_event_delete.setOnClickListener {
                AlertDialog.Builder(context!!)
                    .setTitle("Delete event?")
                    .setPositiveButton("OK") { dialogInterface: DialogInterface, i: Int ->
                        it.isClickable = false
                        db.eventDao().delete(event)
                        db.eventDao().deleteAssociations(event.ID)
                        alertDialog.dismiss()
                        newEventItem.animate()
                            .alpha(0f)
                            .withEndAction {
                                newEventItem.visibility = View.GONE
                                layout_event_items.removeView(newEventItem)
                            }
                    }
                    .setNegativeButton("Cancel") { dialogInterface: DialogInterface, i: Int -> }
                    .create()
                    .show()
            }

            alertDialog.show()
        }

        newEventItem.alpha = 0f
        newEventItem.animate().alpha(1f)

        layout_event_items.addView(newEventItem)
        return newEventItem
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createEventListDialog(event: Event, db: HangTimeDB): View {
        // inflate the dialog view
        val dialogView = layoutInflater.inflate(R.layout.item_event_detail, null)
        val nameEdit = dialogView.text_event_name_edit
        dialogView.id = View.generateViewId()

        // close keyboard on dialog touch
        dialogView.setOnTouchListener { v, event ->
            if (v != null) {
                val imm = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
            }
            true
        }

        // configure title editor
        nameEdit.setText(event.name)
        nameEdit.requestFocus()

        // populate attendee/available list
        dialogView.layout_event_guests.removeAllViews()
        dialogView.layout_event_availables.removeAllViews()
        for (contact in db.contactDao().getAll()){
            val linked = db.contactDao().countEvents(contact.ID, event.ID) > 0
            configureContactItem(dialogView, linked, contact, event, db)
        }

        // open the soft keyboard for new contacts
        if (event.name.isBlank()) {
            nameEdit.postDelayed({
                val imm = this@ViewEvents.activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(nameEdit, 0)
            },200)
        }

        return dialogView
    }

    private fun configureContactItem(dialogView: View, linked: Boolean, contact: Contact, event: Event, db: HangTimeDB): View{
        if (linked) {
            val guestItem = layoutInflater.inflate(R.layout.item_event_detail_guest, null)
            guestItem.text_event_guest_name.text = contact.name

            // configure buttons
            guestItem.button_event_guest_remove.setOnClickListener {
                db.contactDao().unlinkEvent(contact.ID, event.ID)
                dialogView.layout_event_guests.removeView(guestItem)
                configureContactItem(dialogView,false, contact, event, db)
            }

            dialogView.layout_event_guests.addView(guestItem)
            return guestItem

        } else {
            val button = Button(context).apply {
                id = View.generateViewId()

                minimumWidth = 0
                minWidth = 0
                minimumHeight = 0
                minHeight = 0
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12f)
                text = contact.name

                // convert to guest on click
                setOnClickListener {
                    db.contactDao().linkEvent(contact.ID, event.ID)
                    dialogView.layout_event_availables.removeView(this)

                    configureContactItem(dialogView,true, contact, event, db)
                }
            }

            dialogView.layout_event_availables.addView(button)
            return button
        }
    }


    companion object {
        @JvmStatic
        fun newInstance(): ViewEvents {
            instance = ViewEvents().apply {
                arguments = Bundle().apply {

                }
            }

            return instance!!
        }

        private var instance: ViewEvents? = null
        fun getInstance(): ViewEvents {
            return instance ?: newInstance()
        }
    }
}