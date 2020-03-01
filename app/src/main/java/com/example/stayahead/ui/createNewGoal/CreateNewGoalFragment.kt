package com.example.stayahead.ui.createNewGoal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.marginStart
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.stayahead.R

class CreateNewGoalFragment : Fragment() {

    private lateinit var toolsViewModel: CreateNewGoalViewModel

    private lateinit var linearLayout: LinearLayout
    private lateinit var tvDateAndTime: TextView
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        toolsViewModel =
            ViewModelProviders.of(this).get(CreateNewGoalViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_create_new_goal, container, false)
        val etGoalName: EditText = root.findViewById(R.id.etGoalName)
        val btnDueDate: Button = root.findViewById(R.id.btnPickDueDate)
        val btnDueTime: Button = root.findViewById(R.id.btnPickDueTime)
        val btnAddCheckpoint: Button = root.findViewById(R.id.btnAddCheckpoint)
        tvDateAndTime = root.findViewById(R.id.tvDueDate)
        linearLayout = root.findViewById(R.id.lvCheckpoints)

        btnAddCheckpoint.setOnClickListener{

            var etParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT,3f)

            val editText = EditText(root.context)
            editText.layoutParams = etParams
            editText.hint = "Enter a checkpoint!"
            editText.setPadding(32,8,32,64)
            editText.setOnLongClickListener {
                removeItem(editText)

            }
            //tableRow.addView(editText)
            linearLayout.addView(editText)
        }

        return root
    }

    //TODO: Create dialog boxes for remove item and picking date and time
    fun removeItem(et: EditText): Boolean{
        linearLayout.removeView(et)
        return true
    }
}