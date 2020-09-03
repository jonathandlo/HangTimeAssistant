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
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import com.example.hangtimeassistant.*
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import kotlinx.android.synthetic.main.fragment_categories.*
import kotlinx.android.synthetic.main.item_category.view.*
import kotlinx.android.synthetic.main.item_category_detail.view.*

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
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        listCategories()

        // configure the add categories button
        button_cat_add.setOnClickListener {
            val db = HangTimeDB.getDatabase(this.context!!)
            val categoryView = addItem(db.categoryDao().getRow(db.categoryDao().insert(Category())), db)
            categoryView.button_cat_list.performClick()
        }
    }

    private fun listCategories(){
        // populate the view with category entries
        val db = HangTimeDB.getDatabase(this.context!!)
        layout_cat_items.removeAllViews()

        for (category in db.categoryDao().getAll()){
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
        val listButton = newCatItem.button_cat_list
        listButton.setOnClickListener {
            val dialogView = createCategoryListDialog(newCatItem, category, db)
            val alertDialog = AlertDialog.Builder(context!!, R.style.Theme_MaterialComponents_Light_Dialog_Alert)
                .setView(dialogView)
                .setPositiveButton("Close") { dialogInterface: DialogInterface, i: Int ->
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


        updateItem(newCatItem, category, db)
        newCatItem.alpha = 0f
        newCatItem.animate().alpha(1f)

        layout_cat_items.addView(newCatItem)
        return newCatItem
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createCategoryListDialog(newCatItem: View, category: Category, db: HangTimeDB): View {
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
        nameEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (nameEdit.hasFocus()) {
                    db.categoryDao().update(category.apply { name = s.toString() })
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
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
            nameEdit.postDelayed(
                {
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