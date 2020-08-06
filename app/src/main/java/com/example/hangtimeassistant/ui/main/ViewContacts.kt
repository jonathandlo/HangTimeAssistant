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
import androidx.fragment.app.Fragment
import com.example.hangtimeassistant.Contact
import com.example.hangtimeassistant.IDGen
import com.example.hangtimeassistant.Model
import com.example.hangtimeassistant.R
import com.google.android.flexbox.FlexboxLayout
import kotlinx.android.synthetic.main.fragment_contact.*
import kotlinx.android.synthetic.main.item_contact.view.*
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
        // populate the view with reminders
        layout_cont.removeAllViews()

        for (i in Model.contacts.values){
            layout_cont.addView(LayoutInflater.from(this.context).inflate(R.layout.item_contact, null).apply {
                // show details
                this.text_phone.text = i.phoneNum.toString()
                this.text_address.text = i.address
                this.text_fb.text = i.FBUrl
                this.text_ig.text = i.IGUrl

                // TODO: show reminders

                // show categories
                this.flexbox_categories.removeAllViews()
                for (j in i.CategoryIDs) {
                    this.flexbox_categories.addView(Button(context).apply {
                        this.minimumWidth = 0
                        this.minWidth = 0
                        this.minimumHeight = 0
                        this.minHeight = 0
                        this.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12f)
                        this.text = Model.categories[j]?.name ?: ""

                        var drawable = DrawableCompat.wrap(this.background);
                        DrawableCompat.setTint(drawable, Model.categories[j]?.color ?: Color.WHITE)
                    })
                }
            })
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