package com.example.hangtimeassistant.ui.main

import android.graphics.Color
import android.os.Bundle
import android.provider.DocumentsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import com.example.hangtimeassistant.*
import kotlinx.android.synthetic.main.fragment_categories.*
import kotlinx.android.synthetic.main.item_category.view.*
import kotlinx.android.synthetic.main.item_contact.view.*

/**
 * A placeholder fragment containing a simple view.
 */
class ViewCategories : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_categories, container, false)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listCategories()
    }

    private fun listCategories(){
        // populate the view with reminders
        layout_cat_items.removeAllViews()

        for (i in Model.contacts.values){
            layout_cat_items.addView(LayoutInflater.from(this.context).inflate(R.layout.item_category, null).apply {
                this.text_cat_name.text = (Math.random() * 10).toInt().toString()

                var drawable = DrawableCompat.wrap(this.button_cat_color.background);
                DrawableCompat.setTint(drawable, Color.argb(255, (Math.random() * 256).toInt(), (Math.random() * 256).toInt(), (Math.random() * 256).toInt()))
            })
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): ViewCategories {
            return ViewCategories().apply {
                arguments = Bundle().apply {

                }
            }
        }
    }
}