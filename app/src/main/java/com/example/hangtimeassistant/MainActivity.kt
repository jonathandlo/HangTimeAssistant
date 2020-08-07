package com.example.hangtimeassistant

import android.graphics.Color
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.example.hangtimeassistant.ui.main.SectionsPagerAdapter
import kotlin.math.pow

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
		// reminders
		for (i in 1..3){
			val id = IDGen.nextID()
			Model.reminders[id] = Reminder(id)
		}

		// events
		for (i in 1..3){
			val id = IDGen.nextID()
			Model.events[id] = Event(id)
		}

		// categories
		for (i in 1..9){
			val id = IDGen.nextID()
			Model.categories[id] = Category(id).apply {
				val alphabet: List<Char> = ('a'..'z') + (' ') + (' ')
				this.name = List((Math.random() * 10).toInt() + 3) { alphabet.random() }.joinToString("")
				this.color = Color.HSVToColor(floatArrayOf(Math.random().toFloat() * 360f, 0.6f, 0.8f))
			}
		}

		// contacts
		for (i in 1..20){
			val id = IDGen.nextID()
			Model.contacts[id] = Contact(id).apply {
				for (j in Model.categories) {
					if (Math.random() > 0.7) this.CategoryIDs.add(j.key)
				}

				this.name = "John Doe " + i
			}
		}
	}
}