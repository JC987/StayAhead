package com.example.stayahead.ui.stats

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.example.stayahead.R
import com.example.stayahead.ui.stats.StatsViewModel
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry

class StatsFragment : Fragment() {

    companion object {
        fun newInstance() = StatsFragment()
    }

    private lateinit var viewModel: StatsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_stats, container, false)
        val pieChart = root.findViewById<PieChart>(R.id.pieChart)

        val pieEntryList = listOf<PieEntry>(PieEntry(80f),PieEntry(20f))


        val pieDataSet = PieDataSet(pieEntryList,"label!")
        pieDataSet.colors = listOf(root.context.resources.getColor(R.color.colorAccentLight5),root.context.resources.getColor(R.color.colorPrimaryLight5))

        val pieData = PieData(pieDataSet)

        pieChart.data = pieData
        pieChart.invalidate()
        return root;
    }

   /* override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(StatsViewModel::class.java)
        // TODO: Use the ViewModel
        val pieChart =
    }*/

}
