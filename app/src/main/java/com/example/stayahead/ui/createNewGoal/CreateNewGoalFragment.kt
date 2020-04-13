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
    private val TAG = "CreateNewGoalFragment:"
    private var goalDate = ""
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        toolsViewModel =
            ViewModelProviders.of(this).get(CreateNewGoalViewModel::class.java)
        root = inflater.inflate(R.layout.fragment_create_new_goal, container, false)
        val btnDueDate: Button = root.findViewById(R.id.btnPickDueDate)
        btnSubmit = root.findViewById(R.id.btnSubmitNewGoal)
        val btnAddCheckpoint: Button = root.findViewById(R.id.btnAddCheckpoint)
        tvDateAndTime = root.findViewById(R.id.tvDueDate)
        layout = root.findViewById(R.id.lvCheckpoints)
        cpList = ArrayList()


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
        val db = DatabaseHelper(root.context)
        val id = (db.getGoalDBCount() + 1)
        Log.d(TAG,"id is "+ id)
        // can get a view by its id name


        newGoal = Goal(etGoalName.text.toString(),"0.0", goalDate,
            false, id)
        for (i:Int in 0 until cpList.size){
            val et =  cpList.get(i).getChildAt(0) as EditText
            val d = (cpList.get(i).getChildAt(1) as Button).text.toString()
            val ck = Checkpoint(et.text.toString(),d,"time",false, id)
            newGoal.addCheckpoint(ck)
            db.addCheckpointData(ck)
            Log.d(TAG, "${et.text}")
        }

        db.addGoalData(newGoal)
        parentFragmentManager.popBackStack()
    }


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
        dialog.show()

    }

    fun datePickerDialog(btn:Button){
        val view = View.inflate(root.context,R.layout.dialog_datepicker, null)
        val dp = view.findViewById<DatePicker>(R.id.datePicker)
        var tmpDate =""
        val dialog = AlertDialog.Builder(root.context)
        dialog.setView(view)
        dialog.setTitle("Date Picker")
        dialog.setPositiveButton("Confirm"
        ) { _:DialogInterface, _:Int ->
            if((dp.month + 1)<10)
                tmpDate = "${dp.year}-0${(dp.month + 1)}-${dp.dayOfMonth}"
            else
                tmpDate = "${dp.year}-${(dp.month + 1)}-${dp.dayOfMonth}"

            if(btn.id == R.id.btnPickDueDate){
                goalDate = tmpDate
                val tmp = "Due Date is: " + goalDate
                tvDateAndTime.text = tmp
            }
            else
                btn.text = tmpDate
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


        btnDateCk.setOnClickListener {
            datePickerDialog(btnDateCk)
        }

        tableRow.layoutParams = trParams

        btnDateCk.text = "Date"

        btnDateCk.layoutParams = btnParams

        editText.layoutParams = etParams
        editText.hint = "Enter a checkpoint!"
        editText.setPadding(32,8,32,64)

        tableRow.addView(editText)
        tableRow.addView(btnDateCk)
        layout.addView(tableRow)

        editText.setOnLongClickListener {
            removeItemDialog(tableRow)
            true
        }
        cpList.add(tableRow)

    }
}