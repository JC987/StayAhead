package com.example.stayahead.ui.createNewGoal

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.children
import androidx.core.view.marginStart
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.stayahead.Checkpoint
import com.example.stayahead.DatabaseHelper
import com.example.stayahead.Goal
import com.example.stayahead.R
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_create_new_goal.*
import java.sql.Time

class CreateNewGoalFragment : Fragment() {

    private lateinit var toolsViewModel: CreateNewGoalViewModel
    private lateinit var root: View
    private lateinit var layout: TableLayout
    private lateinit var tvDateAndTime: TextView
    private lateinit var newGoal: Goal
    private lateinit var cpList: ArrayList<TableRow>
    private lateinit var btnSubmit: Button
   // private lateinit var btnDueTime: Button
    private val TAG = "CreateNewGoalFragment:"
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        toolsViewModel =
            ViewModelProviders.of(this).get(CreateNewGoalViewModel::class.java)
        root = inflater.inflate(R.layout.fragment_create_new_goal, container, false)
        val etGoalName: EditText = root.findViewById(R.id.etGoalName)
        val btnDueDate: Button = root.findViewById(R.id.btnPickDueDate)
        val btnDueTime: Button = root.findViewById(R.id.btnPickDueTime)
        btnSubmit = root.findViewById(R.id.btnSubmitNewGoal)
        val btnAddCheckpoint: Button = root.findViewById(R.id.btnAddCheckpoint)
        tvDateAndTime = root.findViewById(R.id.tvDueDate)
        layout = root.findViewById(R.id.lvCheckpoints)
        cpList = ArrayList()

        btnDueTime.setOnClickListener {
            timePickerDialog(btnDueTime)
            //btnDueTime.text = str
        }
        btnDueDate.setOnClickListener {
            datePickerDialog(btnDueDate)
        }
        btnSubmit.setOnClickListener{
            submit()
        }
        btnAddCheckpoint.setOnClickListener{
            createCheckpoint()
        }

        return root
    }

    private fun submit() {
        newGoal = Goal(etGoalName.text.toString(),"0%",false)
        for (i:Int in 0..cpList.size - 1){
            val et =  cpList.get(i).getChildAt(0) as EditText
            val d = (cpList.get(i).getChildAt(1) as Button).text.toString()
            val t = (cpList.get(i).getChildAt(2) as Button).text.toString()
            newGoal.addCheckpoint(Checkpoint(et.text.toString(),d,t,false))
            Log.d("cpList: ", "${et.text}")
        }
        val db = DatabaseHelper(root.context)
        db.addData(newGoal)
        parentFragmentManager.popBackStack()
    }



    //TODO: Create dialog boxes for remove item and picking date and time
    fun removeItemDialog(tr: TableRow){
        val dialog = AlertDialog.Builder(root.context)
        dialog.setTitle("Delete Checkpoint")
        dialog.setMessage("Do you want to delete this checkpoint")
        dialog.setPositiveButton( "Yes", DialogInterface.OnClickListener { dialogInterface, i ->
            Log.d(TAG, "Deleting a checkpoint row")
            layout.removeView(tr)
            cpList.remove(tr)
            Toast.makeText(root.context,"Deleted!",Toast.LENGTH_SHORT).show()

        })
        dialog.setNegativeButton("No", DialogInterface.OnClickListener { dialogInterface, i ->
            Toast.makeText(root.context,"Canceled!",Toast.LENGTH_SHORT).show()
        })
        //?dialog.create()
        dialog.show()

    }

    fun timePickerDialog(btn:Button){
        val view = View.inflate(root.context,R.layout.dialog_timepicker, null)
        val tp = view.findViewById<TimePicker>(R.id.timePicker)

        val dialog = AlertDialog.Builder(root.context)
        dialog.setView(view)
        dialog.setTitle("TimePicker!")
        dialog.setPositiveButton("Confirm"
        ) { _:DialogInterface, _:Int ->
            btn.text = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                "${tp.hour}:${tp.minute}"
            else
                "${tp.currentHour}:${tp.currentMinute}"

        }
        dialog.create()
        dialog.show()

    } fun datePickerDialog(btn:Button){
        val view = View.inflate(root.context,R.layout.dialog_datepicker, null)
        val dp = view.findViewById<DatePicker>(R.id.datePicker)

        val dialog = AlertDialog.Builder(root.context)
        dialog.setView(view)
        dialog.setTitle("TimePicker!")
        dialog.setPositiveButton("Confirm"
        ) { _:DialogInterface, _:Int ->
            btn.text = "${dp.year}${dp.month}${dp.dayOfMonth}"

        }
        dialog.create()
        dialog.show()

    }

    fun createCheckpoint(){
        val tableRow = TableRow(root.context)
        val trParams = TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,TableLayout.LayoutParams.WRAP_CONTENT)

        val etParams = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 10f);
        val editText = EditText(root.context)

        val btnDateCk = Button(root.context)
        val btnParams = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1f);

        val btnTimeCk = Button(root.context)
        btnTimeCk.setOnClickListener {
            timePickerDialog(btnTimeCk)
        }
        btnDateCk.setOnClickListener {
            datePickerDialog(btnDateCk)
        }

        tableRow.layoutParams = trParams

        btnDateCk.text = "Date"
        btnTimeCk.text = "Time"

        btnDateCk.layoutParams = btnParams
        btnTimeCk.layoutParams = btnParams

        editText.layoutParams = etParams
        editText.hint = "Enter a checkpoint!"
        editText.setPadding(32,8,32,64)

        tableRow.addView(editText)
        tableRow.addView(btnDateCk)
        tableRow.addView(btnTimeCk)

        layout.addView(tableRow)

        editText.setOnLongClickListener {
            removeItemDialog(tableRow)
            true
        }
        cpList.add(tableRow)

    }
}