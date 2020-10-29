package com.example.hangtimeassistant.ui.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import com.example.hangtimeassistant.*
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import kotlinx.android.synthetic.main.fragment_categories.*
import kotlinx.android.synthetic.main.fragment_contact.*
import kotlinx.android.synthetic.main.item_category.view.*
import kotlinx.android.synthetic.main.item_category_detail.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * A placeholder fragment containing a simple view.
 */
class ViewCategories : Fragment() {
    var numSearches = 0
    var displayedQuery = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_categories, container, false)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        listCategories()

        // configure the search box
        textinput_cat_search.doOnTextChanged { text, start, before, count ->
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
                    listCategories(thisQuery)
                    displayedQuery = thisQuery
                }
            }
        }

        // configure the add categories button
        button_cat_add.setOnClickListener {
            val db = HangTimeDB.getDatabase(this.context!!)
            val categoryView = addItem(db.categoryDao().getRow(db.categoryDao().insert(Category())), db)
            categoryView.button_cat_detail.performClick()
        }
    }

    private fun listCategories(searchTerm: String = ""){
        // populate the view with category entries
        val db = HangTimeDB.getDatabase(this.context!!)
        layout_cat_items.removeAllViews()

        for (category in db.categoryDao().getAll()){
            if (searchTerm.isNotEmpty()
                && !category.name.contains(searchTerm)) continue

            addItem(category, db)
        }
    }

    private fun addItem(category: Category, db: HangTimeDB) : View {
        val newCatItem = layoutInflater.inflate(R.layout.item_category, null)
        newCatItem.id = View.generateViewId()

        // configure name textbox
        val textName = newCatItem.text_cat_name
        DrawableCompat.setTint(DrawableCompat.wrap(textName.background).mutate(), Color.TRANSPARENT)
        textName.id = View.generateViewId()
        textName.text = category.name

        // configure color button style
        val colorButton = newCatItem.button_cat_color
        DrawableCompat.setTint(DrawableCompat.wrap(colorButton.background).mutate(), category.color)
        colorButton.setOnClickListener {
            ColorPickerDialogBuilder
                .with(context)
                .setTitle("Choose a category color")
                .initialColor(category.color)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(9)
                .lightnessSliderOnly()
                .showColorPreview(true)
                .setOnColorSelectedListener { }
                .setPositiveButton("OK") { dialog, selectedColor, allColors ->
                    category.color = selectedColor
                    db.categoryDao().update(category)
                    DrawableCompat.setTint(DrawableCompat.wrap(colorButton.background).mutate(), category.color)
                }
                .setNegativeButton("Cancel") { dialogInterface: DialogInterface, i: Int -> }
                .build()
                .show()
        }

        // configure list button/dialog
        val listButton = newCatItem.button_cat_detail
        listButton.setOnClickListener {
            val dialogView = createCategoryListDialog(category, db)
            val alertDialog = AlertDialog.Builder(context!!, R.style.Theme_MaterialComponents_Light_Dialog_Alert)
                .setView(dialogView)
                .setPositiveButton("Close") { dialogInterface: DialogInterface, i: Int -> }
                .setOnDismissListener {
                    category.name = dialogView.text_cat_name_edit.text.toString()
                    db.categoryDao().update(category)
                    textName.text = category.name
                }
                .create()

            // configure delete button
            dialogView.button_cat_delete.setOnClickListener {
                    AlertDialog.Builder(context!!)
                        .setTitle("Delete category?")
                        .setPositiveButton("OK") { dialogInterface: DialogInterface, i: Int ->
                            it.isClickable = false
                            db.categoryDao().delete(category)
                            db.categoryDao().deleteAssociations(category.ID)
                            alertDialog.dismiss()
                            newCatItem.animate()
                                .alpha(0f)
                                .withEndAction {
                                    newCatItem.visibility = View.GONE
                                    layout_cat_items.removeView(newCatItem)
                                }
                        }
                        .setNegativeButton("Cancel") { dialogInterface: DialogInterface, i: Int -> }
                        .create()
                        .show()
            }

            alertDialog.show()
        }

        newCatItem.alpha = 0f
        newCatItem.animate().alpha(1f)

        layout_cat_items.addView(newCatItem)
        return newCatItem
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createCategoryListDialog(category: Category, db: HangTimeDB): View {
        // inflate the dialog view
        val dialogView = layoutInflater.inflate(R.layout.item_category_detail, null)
        val nameEdit = dialogView.text_cat_name_edit
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
        nameEdit.setText(category.name)
        nameEdit.requestFocus()

        // populate contact lists
        for (contact in db.contactDao().getAll()){
            val button = Button(context).apply {
                id = View.generateViewId()

                minimumWidth = 0
                minWidth = 0
                minimumHeight = 0
                minHeight = 0
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12f)
                text = contact.name

                // configure button based on linked state
                val linked = db.contactDao().countCategories(contact.ID, category.ID) > 0

                configureButton(this, dialogView, linked, contact, category, db)
                if (linked) dialogView.layout_cat_linkedcontacts.addView(this)
                else dialogView.layout_cat_unlinkedcontacts.addView(this)
            }
        }

        // open the soft keyboard for new contacts
        if (category.name.isBlank()) {
            nameEdit.postDelayed({
                val imm = this@ViewCategories.activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(nameEdit, 0)
            },200)
        }

        return dialogView
    }

    private fun configureButton(button: Button, dialogView: View, linked: Boolean, contact: Contact, category: Category, db: HangTimeDB){
        button.apply {
            if (linked) {
                setTextColor(Color.argb(110, 0, 0, 0))
                DrawableCompat.setTint(DrawableCompat.wrap(background).mutate(), category.color)

                // remove association on click
                setOnClickListener {
                    db.contactDao().unlinkCategory(contact.ID, category.ID)
                    dialogView.layout_cat_linkedcontacts.removeView(this)
                    dialogView.layout_cat_unlinkedcontacts.addView(this)

                    configureButton(button, dialogView,false, contact, category, db)
                }
            } else {
                val backColor = Color.rgb(
                    Color.red(category.color) / 4 + 50,
                    Color.green(category.color) / 4 + 50,
                    Color.blue(category.color) / 4 + 50
                )
                setTextColor(Color.argb(55, 255, 255, 255))
                DrawableCompat.setTint(DrawableCompat.wrap(background).mutate(), backColor)

                // add association on click
                setOnClickListener {
                    db.contactDao().linkCategory(contact.ID, category.ID)
                    dialogView.layout_cat_unlinkedcontacts.removeView(this)
                    dialogView.layout_cat_linkedcontacts.addView(this)

                    configureButton(button, dialogView,true, contact, category, db)
                }
            }
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