package com.example.hangtimeassistant.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.hangtimeassistant.*
import kotlinx.android.synthetic.main.fragment_upcoming.*

/**
 * A placeholder fragment containing a simple view.
 */
class ViewUpcoming : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_upcoming, container, false)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listUpcoming()
    }

    private fun listUpcoming(){
        // populate the view with reminders
        layout_remind_items.removeAllViews()

        //TODO: algorithm for showing upcoming
    }

    companion object {
        @JvmStatic
        fun newInstance(): ViewUpcoming {
            return ViewUpcoming().apply {
                arguments = Bundle().apply {

                }
            }
        }
    }
}