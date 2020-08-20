package com.example.hangtimeassistant.ui.main

import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.hangtimeassistant.*
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerClickListener
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import kotlinx.android.synthetic.main.fragment_categories.*
import kotlinx.android.synthetic.main.item_category.view.*

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
            addItem(db.categoryDao().getRow(db.categoryDao().insert(Category())), db)
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

    private fun addItem(category: Category, db: HangTimeDB) {
        val newCatItem = layoutInflater.inflate(R.layout.item_category, null)
        newCatItem.id = View.generateViewId()

        // configure name textbox events
        val nameEdit = newCatItem.textedit_cat_name
        nameEdit.id = View.generateViewId()
        DrawableCompat.setTint(DrawableCompat.wrap(nameEdit.background).mutate(), Color.TRANSPARENT)
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

        // configure color button style
        val colorButton = newCatItem.button_cat_color
        colorButton.id = View.generateViewId()
        colorButton.minimumWidth = colorButton.minimumHeight
        colorButton.minWidth = colorButton.minHeight
        DrawableCompat.setTint(DrawableCompat.wrap(colorButton.background).mutate(), category.color)
        colorButton.setOnClickListener {
            ColorPickerDialogBuilder
                .with(context)
                .setTitle("Choose a Category color")
                .initialColor(category.color)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(9)
                .lightnessSliderOnly()
                .showColorPreview(true)
                .setOnColorSelectedListener {

                }
                .setPositiveButton("ok") { dialog, selectedColor, allColors ->
                    category.color = selectedColor
                    db.categoryDao().update(category)
                    DrawableCompat.setTint(DrawableCompat.wrap(colorButton.background).mutate(), category.color)
                }
                .setNegativeButton("cancel") { dialogInterface: DialogInterface, i: Int ->

                }
                .build()
                .show()
        }

        // configure delete button
        val delButton = newCatItem.button_cat_delete
        delButton.id = View.generateViewId()
        delButton.setOnClickListener {
            it.isClickable = false
            db.categoryDao().delete(category)
            db.categoryDao().deleteAssociations(category.ID)
            newCatItem.animate()
                .alpha(0f)
                .withEndAction {
                    newCatItem.visibility = View.GONE
                    layout_cat_items.removeView(newCatItem)
                }
        }

        newCatItem.alpha = 0f
        newCatItem.animate().alpha(1f)

        layout_cat_items.addView(newCatItem)
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