package com.example.stayahead

class Goal(var goalName: String, var remainingPercentage: String, var date: String, var isFinished: Boolean, var goalId: Int){

    var listOfCheckpoints = ArrayList<Checkpoint>()

    fun addCheckpoint(cp:Checkpoint){
        listOfCheckpoints.add(cp)
    }
    fun getList():ArrayList<Checkpoint>{
        return listOfCheckpoints
    }
    fun removeCheckpoint(cp:Checkpoint){
        listOfCheckpoints.remove(cp)
    }
    fun setName(name:String){
        goalName = name
    }
    fun setPercentage(per:String){
        remainingPercentage = per
    }
    
    fun getDateAsString(): String{
        if(date == "")
            return getDefDateString()
        return date
    }

    fun getDefDateString() : String{
       return "02/28/2020"
   }
}
