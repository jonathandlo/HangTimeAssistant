package com.example.hangtimeassistant.ui.main

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import com.example.hangtimeassistant.*
import kotlinx.android.synthetic.main.fragment_contact.*
import kotlinx.android.synthetic.main.item_contact.view.*
import kotlinx.android.synthetic.main.item_contact_collapsible.view.*
import kotlinx.android.synthetic.main.item_reminder_config.view.*
import java.util.ArrayList


/**
 * A placeholder fragment containing a simple view.
 */
class ViewContacts : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_contact, container, false)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listContacts()

        // configure the add contact button
        button_cont_add.setOnClickListener {
            val db = HangTimeDB.getDatabase(this.context!!)
            val contactItem = addItem(db.contactDao().getRow(db.contactDao().insert(Contact())), db)
            layout_cont_items.addView(contactItem)
        }
    }

    private fun listContacts(){
        val db = HangTimeDB.getDatabase(context!!)
        layout_cont_items.removeAllViews()

        // populate the view with contacts
        for (contact in db.contactDao().getAll()){
            val contactItem = addItem(contact, db)
            layout_cont_items.addView(contactItem)
        }
    }

    private fun addItem(contact: Contact, db: HangTimeDB): View? {
        // inflate the contact xml
        val contactItem = layoutInflater.inflate(R.layout.item_contact, null)

        // customize the name edittext
        val nameEdit = contactItem.text_cont_name
        nameEdit.id = View.generateViewId()
        DrawableCompat.setTint(DrawableCompat.wrap(nameEdit.background).mutate(), Color.TRANSPARENT)
        nameEdit.setText(contact.name)
        nameEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (nameEdit.hasFocus()) {
                    db.contactDao().update(contact.apply { name = s.toString() })
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // attach on-click animation events
        contactItem.img_cont_chevron.setOnClickListener {
            if (contactItem.findViewById<LinearLayout>(R.id.layout_cont_collapsible) == null) {
                // one first click, create the collapsible view
                val collapsible = layoutInflater.inflate(R.layout.item_contact_collapsible, null)
                collapsible.id = View.generateViewId()
                collapsible.visibility = View.GONE
                collapsible.alpha = 0f

                // update details
                collapsible.text_phone.setText(contact.phoneNum)
                collapsible.text_address.setText(contact.address)
                collapsible.text_fb.setText(contact.FBUrl)
                collapsible.text_ig.setText(contact.IGUrl)

                // add text changed listeners
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

                // TODO: show reminders
                setupReminderItem(collapsible)

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

                // configure delete button
                val delButton = collapsible.button_cont_delete
                delButton.id = View.generateViewId()
                delButton.setOnClickListener {
                    it.isClickable = false
                    db.contactDao().delete(contact)
                    db.contactDao().deleteAssociations(contact.ID)
                    contactItem.animate()
                        .alpha(0f)
                        .withEndAction {
                            contactItem.visibility = View.GONE
                            layout_cont_items.removeView(contactItem)
                        }
                }

                collapsible.id = R.id.layout_cont_collapsible
                contactItem.layout_cont_item_main.addView(collapsible)
            }

            // collapsible view is created, animate
            val collapsible = contactItem.layout_cont_item_main.layout_cont_collapsible
            if (collapsible.visibility == View.VISIBLE) {
                // if visible, hide the view
                collapsible.animate()
                    .alpha(0f)
                    .withEndAction {
                        collapsible.visibility = View.GONE
                    }

                it.animate()
                    .rotation(0f)
            } else {
                // if hidden, show the view
                collapsible.visibility = View.VISIBLE
                collapsible.animate()
                    .alpha(1f)

                it.animate()
                    .rotation(180f)
            }
        }
        return contactItem
    }

    private fun setupReminderItem(pParentView: View) {
        // inflate remaining controls
        pParentView.daypicker_rem

        // display reminder controls only if reminder checked
        pParentView.alpha = 0f
        pParentView.table_rem_details.visibility = View.GONE
        pParentView.cb_cont_reminder.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked){
                pParentView.table_rem_details.visibility = View.VISIBLE
                pParentView.table_rem_details.animate().alpha(1f)
            }
            else {
                pParentView.table_rem_details.animate().alpha(1f)
                pParentView.table_rem_details.animate()
                    .alpha(0f)
                    .withEndAction {
                        pParentView.table_rem_details.visibility = View.GONE
                    }
            }
        }

        // display options based on recurrence type
        pParentView.spinner_recurrence_type.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0 /* with no recurrence */ -> {
                        pParentView.row_rem_recurrence.visibility = View.GONE
                        pParentView.row_rem_datepick.visibility = View.VISIBLE
                        pParentView.row_rem_weekday.visibility = View.GONE
                        pParentView.row_rem_randomness.visibility = View.GONE
                        pParentView.row_rem_skip.visibility = View.GONE
                    }
                    1 /* at regular intervals */ -> {
                        pParentView.row_rem_recurrence.visibility = View.GONE
                        pParentView.row_rem_datepick.visibility = View.GONE
                        pParentView.row_rem_weekday.visibility = View.GONE
                        pParentView.row_rem_randomness.visibility = View.VISIBLE
                        pParentView.row_rem_skip.visibility = View.VISIBLE
                    }
                    2 /* on day(s) of week */ -> {
                        pParentView.row_rem_recurrence.visibility = View.GONE
                        pParentView.row_rem_datepick.visibility = View.GONE
                        pParentView.row_rem_weekday.visibility = View.VISIBLE
                        pParentView.row_rem_randomness.visibility = View.VISIBLE
                        pParentView.row_rem_skip.visibility = View.VISIBLE
                    }
                    3 /* on day(s) of month */ -> {
                        pParentView.row_rem_recurrence.visibility = View.GONE
                        pParentView.row_rem_datepick.visibility = View.GONE
                        pParentView.row_rem_weekday.visibility = View.VISIBLE
                        pParentView.row_rem_randomness.visibility = View.VISIBLE
                        pParentView.row_rem_skip.visibility = View.VISIBLE
                    }
                    4 /* on day(s) of year */ -> {
                        pParentView.row_rem_recurrence.visibility = View.GONE
                        pParentView.row_rem_datepick.visibility = View.GONE
                        pParentView.row_rem_weekday.visibility = View.VISIBLE
                        pParentView.row_rem_randomness.visibility = View.VISIBLE
                        pParentView.row_rem_skip.visibility = View.VISIBLE
                    }
                    else  -> { }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
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