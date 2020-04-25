package com.example.stayahead.ui.settings

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.stayahead.DatabaseHelper
import com.example.stayahead.R

class SettingsFragment : Fragment() {

    //private lateinit var slideshowViewModel: SettingsViewModel
    private lateinit var root: View
    private lateinit var btnTimePicker:Button
    private lateinit var sharedPreferences: SharedPreferences
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_settings, container, false)
        val btnTruncate: Button = root.findViewById(R.id.btnDropTables)
        btnTimePicker = root.findViewById(R.id.btnSettingsTimePicker)
        sharedPreferences = root.context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        btnTimePicker.text = "${sharedPreferences.getInt("notification_time_hour",9)}:${sharedPreferences.getInt("notification_time_minute",0)}"
        val db = DatabaseHelper(root.context)
        btnTruncate.setOnClickListener {
            db.truncateTables()
        }
        btnTimePicker.setOnClickListener {
            createTimePickerDialog()
        }

        return root
    }

    private fun createTimePickerDialog(){
        val view = View.inflate(root.context, R.layout.dialog_timepicker, null)
        val tp = view.findViewById<TimePicker>(R.id.timePicker)

        val dialog = AlertDialog.Builder(root.context)
        dialog.setTitle("Set notification time")
        dialog.setView(view)
        dialog.setPositiveButton("Confirm") { _: DialogInterface, _: Int ->
            if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                btnTimePicker.text = "${tp.currentHour}:${tp.currentMinute}"
                val editor = sharedPreferences.edit()
                editor.putInt("notification_time_hour",tp.currentHour)
                editor.putInt("notification_time_minute",tp.currentMinute)
                editor.apply()
            }
            else {
                btnTimePicker.text = "${tp.hour}:${tp.minute}"
                val editor = sharedPreferences.edit()
                editor.putInt("notification_time_hour",tp.hour)
                editor.putInt("notification_time_minute",tp.minute)
                editor.apply()
            }

            Toast.makeText(root.context,"time updated",Toast.LENGTH_SHORT).show()
        }

        dialog.create()
        dialog.show()
    }
}












