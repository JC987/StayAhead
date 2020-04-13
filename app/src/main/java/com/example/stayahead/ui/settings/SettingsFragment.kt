package com.example.stayahead.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.stayahead.DatabaseHelper
import com.example.stayahead.R

class SettingsFragment : Fragment() {

    private lateinit var slideshowViewModel: SettingsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_settings, container, false)
        val button: Button = root.findViewById(R.id.btnDropTables)
        val db = DatabaseHelper(root.context)
        button.setOnClickListener {
            db.truncateTables()
        }
        return root
    }
}