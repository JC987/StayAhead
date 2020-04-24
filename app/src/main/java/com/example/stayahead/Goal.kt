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
        return date
    }


}
