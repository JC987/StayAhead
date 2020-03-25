package com.example.stayahead

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.ArrayList
//TODO: REMOVE THIS CLASS
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val rvList = findViewById<RecyclerView>(R.id.rvList)
        val listItems = ArrayList<Goal>()
        var cpList = ArrayList<Checkpoint>()



    }
}
