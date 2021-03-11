package hypr.social.hangtimeassistant.ui.main

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
import androidx.lifecycle.lifecycleScope
import hypr.social.hangtimeassistant.*
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import hypr.social.hangtimeassistant.model.Category
import hypr.social.hangtimeassistant.model.Contact
import hypr.social.hangtimeassistant.model.HTAFirestore
import kotlinx.android.synthetic.main.fragment_categories.*
import kotlinx.android.synthetic.main.item_category.view.*
import kotlinx.android.synthetic.main.item_category_detail.view.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * A placeholder fragment containing a simple view.
 */
class ViewCategories : Fragment() {
    public var needsUpdating = true

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

        // configure the search box
        textinput_cat_search.doOnTextChanged { text, start, before, count ->
            numSearches++
            val searchNumber = numSearches
            val thisQuery = text.toString().trim()

            // leave if these results are already being displayed
            if (thisQuery == displayedQuery) return@doOnTextChanged
            println("new search query: $thisQuery")

            lifecycleScope.launch(IO) {
                delay(500)

                // leave if this is not the latest search
                if (searchNumber != numSearches) return@launch
                println("launching search: $thisQuery")

                // trigger a new search
                withContext(Main) {
                    listCategories(thisQuery)
                    displayedQuery = thisQuery
                }
            }
        }

        // configure the add categories button
        button_cat_add.setOnClickListener {
            lifecycleScope.launch(IO) {
                val categoryView = addItem(HTAFirestore.add(Category()))
                ViewContacts.getInstance().needsUpdating = true
                ViewMap.getInstance().needsUpdating = true

                withContext(Main) {
                    categoryView.button_cat_detail.performClick()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

    }

    override fun onResume() {
        super.onResume()

        if (needsUpdating) lifecycleScope.launch(IO) { listCategories() }
        needsUpdating = false
    }

    private suspend fun listCategories(searchTerm: String = ""){
        // populate the view with category entries
        withContext(Main) { layout_cat_items.removeAllViews() }

        for (category in withContext(IO) { HTAFirestore.getAllCategories() }) {
            if (searchTerm.isNotEmpty()
                && !category.name.contains(searchTerm, true)
            ) continue

            lifecycleScope.launch(IO) {
                addItem(category)
            }
        }
    }

    private suspend fun addItem(category: Category) : View {
        return withContext(Main) {
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
                        DrawableCompat.setTint(DrawableCompat.wrap(colorButton.background).mutate(), category.color)

                        lifecycleScope.launch(IO) {
                            HTAFirestore.update(category)
                            ViewContacts.getInstance().needsUpdating = true
                            ViewMap.getInstance().needsUpdating = true
                        }
                    }
                    .setNegativeButton("Cancel") { dialogInterface: DialogInterface, i: Int -> }
                    .build()
                    .show()
            }

            // configure list button/dialog
            val listButton = newCatItem.button_cat_detail
            listButton.setOnClickListener {
                val dialogView = createCategoryListDialog(category)
                val alertDialog = AlertDialog.Builder(context!!, R.style.Theme_MaterialComponents_Light_Dialog_Alert)
                    .setView(dialogView)
                    .setPositiveButton("Close") { dialogInterface: DialogInterface, i: Int -> }
                    .setOnDismissListener {
                        category.name = dialogView.text_cat_name_edit.text.toString()
                        textName.text = category.name

                        lifecycleScope.launch(IO) {
                            HTAFirestore.update(category)
                            ViewContacts.getInstance().needsUpdating = true
                            ViewMap.getInstance().needsUpdating = true
                        }
                    }
                    .create()

                // configure delete button
                dialogView.button_cat_delete.setOnClickListener {
                    AlertDialog.Builder(context!!)
                        .setTitle("Delete category?")
                        .setPositiveButton("OK") { dialogInterface: DialogInterface, i: Int ->
                            it.isClickable = false

                            lifecycleScope.launch(IO) {
                                HTAFirestore.unlink(category)
                                HTAFirestore.delete(category)
                                ViewContacts.getInstance().needsUpdating = true
                                ViewMap.getInstance().needsUpdating = true
                            }

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
            return@withContext newCatItem
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createCategoryListDialog(category: Category): View {
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
        lifecycleScope.launch(IO) {
            for (contact in HTAFirestore.getAllContacts()) {
                lifecycleScope.launch(IO) {
                    val linked = HTAFirestore.linked(contact, category)

                    withContext(Main) {
                        val button = Button(context).apply {
                            id = View.generateViewId()
                            minimumWidth = 0
                            minWidth = 0
                            minimumHeight = 0
                            minHeight = 0
                            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12f)
                            text = contact.name
                        }

                        // configure button based on linked state
                        configureButton(button, dialogView, linked, contact, category)
                        if (linked) dialogView.layout_cat_linkedcontacts.addView(button)
                        else dialogView.layout_cat_unlinkedcontacts.addView(button)
                    }
                }
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

    private fun configureButton(button: Button, dialogView: View, linked: Boolean, contact: Contact, category: Category){
        button.apply {
            if (linked) {
                setTextColor(Color.argb(110, 0, 0, 0))
                DrawableCompat.setTint(DrawableCompat.wrap(background).mutate(), category.color)

                // remove association on click
                setOnClickListener {
                    dialogView.layout_cat_linkedcontacts.removeView(this)
                    dialogView.layout_cat_unlinkedcontacts.addView(this)

                    lifecycleScope.launch(IO) {
                        HTAFirestore.unlink(contact, category)
                        ViewContacts.getInstance().needsUpdating = true
                        ViewMap.getInstance().needsUpdating = true
                    }

                    configureButton(button, dialogView,false, contact, category)
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
                    dialogView.layout_cat_unlinkedcontacts.removeView(this)
                    dialogView.layout_cat_linkedcontacts.addView(this)

                    lifecycleScope.launch(IO) {
                        HTAFirestore.link(contact, category)
                        ViewContacts.getInstance().needsUpdating = true
                        ViewMap.getInstance().needsUpdating = true
                    }

                    configureButton(button, dialogView,true, contact, category)
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): ViewCategories {
            instance = ViewCategories().apply {
                arguments = Bundle().apply {

                }
            }

            return instance!!
        }

        private var instance: ViewCategories? = null
        fun getInstance(): ViewCategories {
            return instance ?: newInstance()
        }
    }
}