package com.example.hangtimeassistant.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.hangtimeassistant.*
import kotlinx.android.synthetic.main.fragment_events.*

/**
 * A placeholder fragment containing a simple view.
 */
class ViewEvents : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_events, container, false)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listEvents()
    }

    private fun listEvents(){
        // populate the view with reminders
        layout_event_items.removeAllViews()

        for (i in HangTimeDB.getDatabase(this.context!!).eventDao().getAll()){
            layout_event_items.addView(LayoutInflater.from(this.context).inflate(R.layout.item_event, null))
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): ViewEvents {
            return ViewEvents().apply {
                arguments = Bundle().apply {

                }
            }
        }
    }
}