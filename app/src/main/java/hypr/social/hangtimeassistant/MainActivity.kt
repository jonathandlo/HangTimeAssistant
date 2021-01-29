package hypr.social.hangtimeassistant

import android.content.Context
import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import hypr.social.hangtimeassistant.ui.main.SectionsPagerAdapter

class MainActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		HangTimeDB.getDatabase(this@MainActivity)

		val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)

		val viewPager: ViewPager = findViewById(R.id.view_pager)
		viewPager.adapter = sectionsPagerAdapter
		viewPager.offscreenPageLimit = sectionsPagerAdapter.count // updating views will be done manually as needed

		val tabs: TabLayout = findViewById(R.id.tabs)
		tabs.setupWithViewPager(viewPager)
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