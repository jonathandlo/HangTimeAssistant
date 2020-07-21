package com.example.hangtimeassistant.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.example.hangtimeassistant.IDGen
import com.example.hangtimeassistant.Model
import com.example.hangtimeassistant.R
import com.example.hangtimeassistant.Reminder

/**
 * A placeholder fragment containing a simple view.
 */
class ViewUpcoming : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_upcoming, container, false)

        // top label
        val textView: TextView = root.findViewById(R.id.section_label)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listUpcoming()
    }

    private fun listUpcoming(){
        for (i in 1..10)
        {
            val id = IDGen.nextID()
            Model.reminders.put(id, Reminder(id))
        }
    }

    companion object {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private const val ARG_SECTION_NUMBER = "section_number"

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        @JvmStatic
        fun newInstance(): ViewUpcoming {
            return ViewUpcoming().apply {
                arguments = Bundle().apply {

                }
            }
        }
    }
}