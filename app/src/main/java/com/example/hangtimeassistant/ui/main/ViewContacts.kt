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
        // prepare the data
        Model.contacts.clear()

        for (i in 1..20){
            val id = IDGen.nextID()
            Model.contacts[id] = Contact(id)
        }

        // populate the view with reminders
        layout_cont.removeAllViews()

        for (i in Model.contacts.values){
            layout_cont.addView(LayoutInflater.from(this.context).inflate(R.layout.item_contact, null).apply {
                this.flexbox_categories.removeAllViews()
                for (j in i.CategoryIDs) {
                    this.flexbox_categories.addView(Button(context).apply {
                        //val density = this.context.resources.displayMetrics.density
                        this.layoutParams = FlexboxLayout.LayoutParams(FlexboxLayout.LayoutParams.WRAP_CONTENT, FlexboxLayout.LayoutParams.WRAP_CONTENT)
                        this.minimumWidth = 0
                        this.minWidth = 0
                        this.minimumHeight = 0
                        this.minHeight = 0
                        this.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12f)
                        this.text = Model.categories[j]?.name ?:""

                        var drawable = DrawableCompat.wrap(this.background);
                        DrawableCompat.setTint(drawable, Color.argb(255, (Math.random() * 256).toInt(), (Math.random() * 256).toInt(), (Math.random() * 256).toInt()))
                    })
                }
            })
        }
    }

    companion object {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private const val ARG_SECTION_NUMBER = "section_number"

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        @JvmStatic
        fun newInstance(): ViewContacts {
            return ViewContacts().apply {
                arguments = Bundle().apply {

                }
            }
        }
    }
}