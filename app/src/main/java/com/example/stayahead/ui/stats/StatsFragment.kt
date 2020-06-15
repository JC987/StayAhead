package com.example.stayahead.ui.stats

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import com.example.stayahead.DatabaseHelper

import com.example.stayahead.R
import com.example.stayahead.ui.stats.StatsViewModel
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry

class StatsFragment : Fragment() {

    private lateinit var root: View
    private lateinit var spinner: Spinner
    private lateinit var pieChart: PieChart
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_stats, container, false)
        spinner = root.findViewById<Spinner>(R.id.statsSpinner)
        pieChart = root.findViewById<PieChart>(R.id.pieChart)
        val currentStats = listOf("Goals Finished","Checkpoints Completed", "Completion Percent of Goals")

        spinner.adapter = ArrayAdapter(root.context, R.layout.support_simple_spinner_dropdown_item, currentStats)
        val desc = Description()
        desc.text = ""
        pieChart.description = desc


        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> {
                        defChart()
                        //Toast.makeText(root.context, "pos " + position, Toast.LENGTH_SHORT).show()
                    }
                    1 -> {
                        checkpointStats()
                        //Toast.makeText(root.context, "pos " + position, Toast.LENGTH_SHORT).show()
                    }
                    2 -> {
                        finishedGoalStats()
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Code to perform some action when nothing is selected
            }
        }


        return root;
    }

    fun finishedGoalStats(){
        val db = DatabaseHelper(root.context)

        var total = 0
        var num100 = 0
        var num50 = 0
        var num0 = 0
        val c = db.getFinishedGoals(false)
        while(c.moveToNext()){
            if(c.getString(2).toFloat() == 100f){
                num100++
            }
            else if(c.getString(2).toFloat() >= 50f){
                num50++
            }
            else if(c.getString(2).toFloat() >= 0f){
                num0++
            }

            total++
        }
        pieChart.holeRadius = 25f
        pieChart.transparentCircleRadius = 50f
        val pieEntryList = listOf<PieEntry>(PieEntry( num100.toFloat(),"Fully completed"),PieEntry( num50.toFloat(),"At least half completed"),PieEntry( num0.toFloat(),"Less than half completed"))

        val pieDataSet = PieDataSet(pieEntryList,"")

        val legend = pieChart.legend
        legend.isWordWrapEnabled = true
        legend.form = Legend.LegendForm.SQUARE
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        legend.direction = Legend.LegendDirection.LEFT_TO_RIGHT
        legend.textSize = 16f

        pieDataSet.valueTextSize = 16f
        pieDataSet.colors = listOf(root.context.resources.getColor(R.color.colorAccentLight5),root.context.resources.getColor(R.color.colorPrimaryLight5),root.context.resources.getColor(R.color.colorPrimary5))

        val pieData = PieData(pieDataSet)
        pieChart.setEntryLabelTextSize(0f)
        pieChart.data = pieData
        pieChart.animateY(1400, Easing.EaseInOutQuad)

        pieChart.invalidate()

    }

    fun checkpointStats(){
        val db = DatabaseHelper(root.context)

        val n = db.getNumberOFCheckpointsCompleted().toFloat()
        val n2 = db.getNumberOFCheckpointsFailed().toFloat()
        val c = db.getAllCheckpointData(false)
        while(c.moveToNext()){
            Log.d("stats:" , "c " + c.getString(1) +" goal id " + c.getInt(2) + " completed " + c.getInt(4))
        }
        pieChart.holeRadius = 25f
        pieChart.transparentCircleRadius = 50f
        val pieEntryList = listOf<PieEntry>(PieEntry( n,"Finished"),PieEntry( n2,"Failed"))

        val pieDataSet = PieDataSet(pieEntryList,"")

        val legend = pieChart.legend
        legend.isWordWrapEnabled = true
        legend.form = Legend.LegendForm.SQUARE
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        legend.direction = Legend.LegendDirection.LEFT_TO_RIGHT
        legend.textSize = 16f

        pieDataSet.valueTextSize = 16f
        pieDataSet.colors = listOf(root.context.resources.getColor(R.color.colorAccentLight5), root.context.resources.getColor(R.color.colorPrimary5))

        val pieData = PieData(pieDataSet)

        pieChart.data = pieData
        pieChart.animateY(1400, Easing.EaseInOutQuad)
        pieChart.invalidate()

    }

    fun defChart(){
        val db = DatabaseHelper(root.context)
        val totalFinished = db.getFinishedGoals(false).count.toFloat()
        val totalActive = db.getActiveGoalsData(false).count.toFloat()
        val total = (totalActive + totalFinished)

        pieChart.holeRadius = 25f
        pieChart.transparentCircleRadius = 50f
        val pieEntryList = listOf<PieEntry>(PieEntry( (totalFinished),"Finished"),PieEntry( (totalActive),"Incomplete"))

        val pieDataSet = PieDataSet(pieEntryList,"")

        val legend = pieChart.legend
        legend.isWordWrapEnabled = true
        legend.form = Legend.LegendForm.SQUARE
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        legend.direction = Legend.LegendDirection.LEFT_TO_RIGHT
        //legend.orientation = Legend.LegendOrientation.VERTICAL
        legend.textSize = 16f

        pieDataSet.valueTextSize = 16f
        pieDataSet.colors = listOf(root.context.resources.getColor(R.color.colorAccentLight5),root.context.resources.getColor(R.color.colorPrimaryLight5))

        val pieData = PieData(pieDataSet)

        pieChart.data = pieData
        pieChart.animateY(1400, Easing.EaseInOutQuad)
        pieChart.invalidate()
    }
}
