package hypr.social.hangtimeassistant.ui

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.navigation.NavigationView
import hypr.social.hangtimeassistant.FirebaseMigration
import hypr.social.hangtimeassistant.HangTimeDB
import hypr.social.hangtimeassistant.R
import hypr.social.hangtimeassistant.ui.main.SectionsPagerAdapter
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		HangTimeDB.getDatabase(this@MainActivity)

		// set up the top level view hierarchy
		val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)

		val viewPager: ViewPager = findViewById(R.id.view_pager)
		viewPager.adapter = sectionsPagerAdapter
		viewPager.offscreenPageLimit = sectionsPagerAdapter.count // updating views will be done manually as needed

		val tabs: TabLayout = findViewById(R.id.tabs)
		tabs.setupWithViewPager(viewPager)

		// set up event handling
		nav_view.setNavigationItemSelectedListener(this)
	}

	override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
		// close keyboard on outside touch
		if (currentFocus != null) {
			val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
			imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
		}
		return super.dispatchTouchEvent(ev)
	}

	override fun onNavigationItemSelected(item: MenuItem): Boolean {
		when (item.itemId){
			R.id.navitem_main_migrate -> FirebaseMigration.migrationToFirebase(this)
		}

		return true
	}
}