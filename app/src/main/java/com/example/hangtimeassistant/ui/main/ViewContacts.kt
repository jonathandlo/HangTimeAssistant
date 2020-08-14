package com.example.hangtimeassistant.ui.main

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import com.example.hangtimeassistant.*
import kotlinx.android.synthetic.main.fragment_contact.*
import kotlinx.android.synthetic.main.item_contact.*
import kotlinx.android.synthetic.main.item_contact.view.*
import kotlinx.android.synthetic.main.item_contact_collapsible.view.*


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
    }

    private fun listContacts(){
        // populate the view with contacts
        val db = HangTimeDB.getDatabase(context!!)
        layout_cont.removeAllViews()

        for (contact in db.contactDao().getAll()){
            val contactItem = layoutInflater.inflate(R.layout.item_contact, null)

            DrawableCompat.setTint(DrawableCompat.wrap(nameEdit.background).mutate(), Color.TRANSPARENT)
            // attach on-click animation events
            contactItem.text_cont_name.setOnClickListener {
                if (contactItem.findViewById<LinearLayout>(R.id.layout_cont_collapsible) == null){
                    // one first click, create the collapsible view
                    val collapsible = layoutInflater.inflate(R.layout.item_contact_collapsible,null)
                    collapsible.id = View.generateViewId()
                    collapsible.visibility = View.GONE
                    collapsible.alpha = 0f

                    // update details
                    collapsible.text_phone.setText(contact.phoneNum)
                    collapsible.text_address.setText(contact.address)
                    collapsible.text_fb.setText(contact.FBUrl)
                    collapsible.text_ig.setText(contact.IGUrl)

                    // add text changed listeners
                    collapsible.text_phone.addTextChangedListener(object: TextWatcher {
                        override fun afterTextChanged(s: Editable?) {
                            if (collapsible.text_phone.hasFocus()) {
                                db.contactDao().update(contact.apply { phoneNum = s.toString() })
                            }
                        }
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                    })
                    collapsible.text_address.addTextChangedListener(object: TextWatcher {
                        override fun afterTextChanged(s: Editable?) {
                            if (collapsible.text_address.hasFocus()) {
                                db.contactDao().update(contact.apply { address = s.toString() })
                            }
                        }
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                    })
                    collapsible.text_fb.addTextChangedListener(object: TextWatcher {
                        override fun afterTextChanged(s: Editable?) {
                            if (collapsible.text_fb.hasFocus()) {
                                db.contactDao().update(contact.apply { FBUrl = s.toString() })
                            }
                        }
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                    })
                    collapsible.text_ig.addTextChangedListener(object: TextWatcher {
                        override fun afterTextChanged(s: Editable?) {
                            if (collapsible.text_ig.hasFocus()) {
                                db.contactDao().update(contact.apply { IGUrl = s.toString() })
                            }
                        }
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                    })

                    // TODO: show reminders

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
                                    Color.blue(showColor) / 4 + 50)
                                setTextColor(Color.GRAY)
                            }
                            else setTextColor(Color.WHITE)

                            DrawableCompat.setTint(DrawableCompat.wrap(background).mutate(), showColor)

                            // add click handlers
                            setOnClickListener {
                                if (db.contactDao().countCategories(contact.ID, category.ID) > 0) {
                                    // remove association
                                    db.contactDao().unlinkCategory(contact.ID, category.ID)

                                    val backColor = Color.rgb(
                                        Color.red(category.color) / 4 + 50,
                                        Color.green(category.color) / 4 + 50,
                                        Color.blue(category.color) / 4 + 50)
                                    setTextColor(Color.GRAY)
                                    DrawableCompat.setTint(DrawableCompat.wrap(background).mutate(), backColor)
                                }
                                else {
                                    // add association
                                    db.contactDao().linkCategory(contact.ID, category.ID)
                                    DrawableCompat.setTint(DrawableCompat.wrap(background).mutate(), category.color)
                                    setTextColor(Color.WHITE)
                                }

                            }
                        })
                    }

                    collapsible.id = R.id.layout_cont_collapsible
                    contactItem.layout_cont_item_main.addView(collapsible)
                }

                val collapsible = contactItem.layout_cont_item_main.layout_cont_collapsible
                if (collapsible.visibility == View.VISIBLE) {
                    // if visible, hide the view
                    collapsible.animate()
                        .alpha(0f)
                        .withEndAction {
                            collapsible.visibility = View.GONE
                        }
                }
                else {
                    // if hidden, show the view
                    collapsible.visibility = View.VISIBLE
                    collapsible.animate()
                        .alpha(1f)
                }
            }

            layout_cont.addView(contactItem)
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