package com.example.hangtimeassistant

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import com.example.hangtimeassistant.ui.main.SectionsPagerAdapter

class MainActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		loadModel()

		val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)

		val viewPager: ViewPager = findViewById(R.id.view_pager)
		viewPager.adapter = sectionsPagerAdapter

		val tabs: TabLayout = findViewById(R.id.tabs)
		tabs.setupWithViewPager(viewPager)
	}

	private fun loadModel(){
		val db = HangTimeDB.getDatabase(this@MainActivity)

		// skip adding data
		if("2".toInt() == 2) return

		// reminders
		for (i in 1..3){
			db.reminderDao().insert(Reminder())
		}

		// events
		for (i in 1..3){
			db.eventDao().insert(Event())
		}

		// categories
		for (i in 1..9){
			db.categoryDao().insert(Category().apply {
				val alphabet: List<Char> = ('a'..'z') + (' ') + (' ')
				this.name = List((Math.random() * 10).toInt() + 3) { alphabet.random() }.joinToString("")
				this.color = Color.HSVToColor(floatArrayOf(Math.random().toFloat() * 360f, 0.6f, 0.8f))
			})
		}

		// contacts
		for (i in 1..20){
			val contactId = db.contactDao().insert(Contact().apply {
				this.name = "John Doe " + i
			})

			for (j in db.categoryDao().getAll()) {
				if (Math.random() > 0.7)  db.contactDao().linkCategory(contactId, j.ID)
			}
		}
	}

	override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
		// close keyboard on outside touch
		if (currentFocus != null) {
			val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
			imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
		}
		return super.dispatchTouchEvent(ev)
	}
}