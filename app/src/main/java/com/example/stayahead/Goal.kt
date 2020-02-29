package com.example.stayahead

class Goal(val goalName: String, var remainingPercentage: String, var listofCheckpoint: ArrayList<Checkpoint>){
   /* var goalName = "def_name"
    var remainingPercentage = "10%"
    var listOfCheckpoints = ArrayList<Checkpoint>()
*/
    fun getDefDateString() : String{
       return "02/28/2020"
   }
}
