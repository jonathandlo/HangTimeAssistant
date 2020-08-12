package com.example.hangtimeassistant.ui.main

import android.R.attr.button
import android.R.attr.textSize
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.hangtimeassistant.*
import com.google.android.flexbox.FlexboxLayout
import kotlinx.android.synthetic.main.fragment_contact.*
import kotlinx.android.synthetic.main.fragment_contact.view.*
import kotlinx.android.synthetic.main.item_contact.view.*
import kotlinx.android.synthetic.main.item_contact_collapsible.view.*
import kotlin.math.pow


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

        for (i in db.contactDao().loadContacts()){
            val contactItem = layoutInflater.inflate(R.layout.item_contact, null)

            // attach on-click animation events
            contactItem.text_cont_name.setOnClickListener {
                if (contactItem.findViewById<LinearLayout>(R.id.layout_cont_collapsible) == null){
                    // one first click, create the collapsible view
                    val collapsible = layoutInflater.inflate(R.layout.item_contact_collapsible,null)
                    collapsible.id = R.id.layout_cont_collapsible
                    collapsible.visibility = View.GONE
                    collapsible.alpha = 0f

                    // update details
                    collapsible.text_phone.text = i.phoneNum
                    collapsible.text_address.text = i.address
                    collapsible.text_fb.text = i.FBUrl
                    collapsible.text_ig.text = i.IGUrl

                    // TODO: show reminders

                    // show categories
                    collapsible.flexbox_categories.removeAllViews()
                    for (j in db.contactDao().loadCategories(i.ID)) {
                        collapsible.flexbox_categories.addView(Button(context).apply {
                            id = View.generateViewId()

                            minimumWidth = 0
                            minWidth = 0
                            minimumHeight = 0
                            minHeight = 0
                            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12f)
                            text = j.name

                            DrawableCompat.setTint(DrawableCompat.wrap(background), j.color)
                        })
                    }

                    contactItem.layout_cont_item_main.addView(collapsible)
                }

                val collapsible = contactItem.layout_cont_item_main.layout_cont_collapsible
                if (collapsible.visibility == View.VISIBLE){
                    // if visible, hide the view
                    collapsible.animate()
                        .alpha(0f)
                        .withEndAction {
                            collapsible.visibility = View.GONE
                        }
                }
                else{
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