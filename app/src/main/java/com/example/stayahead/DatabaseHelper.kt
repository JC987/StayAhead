package com.example.stayahead

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.database.Cursor
import android.database.CursorIndexOutOfBoundsException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import android.database.DatabaseUtils



class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, GOAL_TABLE_NAME, null, 1) {
    init {
        Log.d("TAG", "DatabaseHelper: NEW DB HELPER")
    }

    override fun onCreate(sqLiteDatabase: SQLiteDatabase) {
        val createGoalTable = "CREATE TABLE " + GOAL_TABLE_NAME + "( ID INTEGER PRIMARY KEY " +
                "AUTOINCREMENT, " + GOAL_COL1 + " TEXT, " + GOAL_COL2 + " TEXT, " + GOAL_COL3 + " DATE Default CURRENT_DATE, " + GOAL_COL4 + " BOOLEAN)"
        Log.d("TAG", "onCreate: table created")
        val createCheckpointTable = "CREATE TABLE " + CHECKPOINT_TABLE_NAME + "( ID INTEGER PRIMARY KEY " +
                "AUTOINCREMENT, " + CHECKPOINT_COL1 + " TEXT, " + CHECKPOINT_COL2 + " INT, " + CHECKPOINT_COL3 + " DATE Default CURRENT_DATE, " + CHECKPOINT_COL4 + " BOOLEAN)"
        Log.d("TAG", "onCreate: table created")

        sqLiteDatabase.execSQL(createGoalTable)

        sqLiteDatabase.execSQL(createCheckpointTable)
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, i: Int, i1: Int) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS $GOAL_TABLE_NAME")
        onCreate(sqLiteDatabase)
    }

    fun getInfo():Cursor{

        val otherTmp = "SELECT name FROM sqlite_master WHERE type='table' AND name=$GOAL_TABLE_NAME"
        val db = this.writableDatabase
        return db.rawQuery(otherTmp,null)

    }

    fun addGoalData(goal: Goal): Boolean {
        val db = this.writableDatabase
        // db.rawQuery("CREATE TABLE " + GOAL_TABLE_NAME, null);
        val contentValues = ContentValues()
        contentValues.put(GOAL_COL1, goal.goalName)
        contentValues.put(GOAL_COL2, goal.remainingPercentage)
        contentValues.put(GOAL_COL4, goal.isFinished)
        val result = db.insert(GOAL_TABLE_NAME, null, contentValues)
        val i: Int = -1
        Log.d("TAG", "added called")
        GOAL_COUNT++
        return result != i.toLong()
    }

    fun getGoalDBCount(): Int{
        val db = this.readableDatabase
        val c = db.rawQuery("SELECT seq FROM SQLITE_SEQUENCE WHERE name = '$GOAL_TABLE_NAME'",null)
        c.moveToFirst()
        return c.getInt(0)
    }
    fun getCheckpointsForGoal(id: Int): Cursor{
        val db = this.writableDatabase
        val c = db.rawQuery("SELECT * FROM $CHECKPOINT_TABLE_NAME WHERE $CHECKPOINT_COL2 = $id",null)
        return c
    }

    fun addCheckpointData(checkpoint: Checkpoint): Boolean {
        val db = this.writableDatabase
        // db.rawQuery("CREATE TABLE " + GOAL_TABLE_NAME, null);
        val contentValues = ContentValues()
        contentValues.put(CHECKPOINT_COL1, checkpoint.checkpointName)
        contentValues.put(CHECKPOINT_COL2, checkpoint.goalId)
        contentValues.put(CHECKPOINT_COL3, checkpoint.date)
        contentValues.put(CHECKPOINT_COL4, checkpoint.isCompleted)
        val result = db.insert(CHECKPOINT_TABLE_NAME, null, contentValues)
        val i: Int = -1
        //db.close()
        Log.d("TAG", "added called")
        return result != i.toLong()
    }

    fun updateCheckpointData(checkpointId: Int, newName: String, newDate: String){
        val db = this.writableDatabase
        // db.rawQuery("CREATE TABLE " + GOAL_TABLE_NAME, null);
        Log.d("TAG","col will be " + newName)
        db.execSQL("UPDATE $CHECKPOINT_TABLE_NAME SET $CHECKPOINT_COL1 = '${newName}', $CHECKPOINT_COL3 = '${newDate}' WHERE ID = ${checkpointId}")
        //c.close()
        //db.close()
    }

    fun updateCheckpointCompleted(id:Int, value:Int){
        val db = this.writableDatabase
        db.execSQL("UPDATE $CHECKPOINT_TABLE_NAME SET $CHECKPOINT_COL4 = $value WHERE ID = $id")
       // db.close()
    }

    fun updateGoalNameAndDate(goalId:Int,newGoalName:String, newGoalDate:String, newPercent:String){
        val db = this.writableDatabase
        db.execSQL("UPDATE $GOAL_TABLE_NAME SET $GOAL_COL1 = '${newGoalName}', $GOAL_COL3 = '${newGoalDate}', $GOAL_COL2 = '${newPercent}' WHERE ID = ${goalId}")
        // db.close()
    }

    fun updateGoalPercentage(id:Int, per:String){
        val db = this.writableDatabase
        db.execSQL("UPDATE $GOAL_TABLE_NAME SET $GOAL_COL2 = $per WHERE ID = $id")
       // db.close()
    }

    fun finishGoal(goalId:Int){
        val db = this.writableDatabase
        db.execSQL("UPDATE $GOAL_TABLE_NAME SET $GOAL_COL4 = 1 WHERE ID = $goalId")
      //  db.close()
    }

    fun getFinishedGoals(): Cursor{
        val db = this.writableDatabase
        val c = db.rawQuery("SELECT * FROM $GOAL_TABLE_NAME  WHERE $GOAL_COL4 = 1", null)
        //db.close() don't close connection!
        return c
    }

    fun deleteGoal(goalId:Int){
        val db = this.writableDatabase
        db.execSQL("DELETE FROM $GOAL_TABLE_NAME WHERE ID = $goalId")
        db.execSQL("DELETE FROM $CHECKPOINT_TABLE_NAME WHERE $CHECKPOINT_COL2 = $goalId")
        db.close()
    }

    /**
     * Returns all data
     * @return
     */
    fun getAllGoalData(desc: Boolean): Cursor {

        val db = this.writableDatabase

        //db.execSQL("CREATE TABLE IF NOT EXISTS "+ GOAL_TABLE_NAME);
        return if (desc) db.rawQuery(
            "SELECT * FROM $GOAL_TABLE_NAME WHERE $GOAL_COL4 = 0 ORDER BY id DESC",
            null
        ) else db.rawQuery("SELECT * FROM $GOAL_TABLE_NAME WHERE $GOAL_COL4 = 0" , null)

    }

    fun getAllCheckpointData(desc: Boolean): Cursor {
        val db = this.writableDatabase

        //db.execSQL("CREATE TABLE IF NOT EXISTS "+ GOAL_TABLE_NAME);
        return if (desc) db.rawQuery(
            "SELECT * FROM $CHECKPOINT_TABLE_NAME ORDER BY id DESC",
            null
        ) else db.rawQuery("SELECT * FROM $CHECKPOINT_TABLE_NAME", null)

    }

    /**
     * Returns all data
     * @return
     */
    fun deleteDB() {
        val db = this.writableDatabase
        db.execSQL("DELETE FROM  $GOAL_TABLE_NAME")
        db.execSQL("DELETE FROM $CHECKPOINT_TABLE_NAME")

       // db.execSQL("DELETE FROM goals")
     //   db.execSQL("DELETE FROM HolyGoals")

    }

    fun deleteItem(title: String) {
        val db = this.writableDatabase
        db.execSQL("DELETE FROM  $GOAL_TABLE_NAME WHERE $GOAL_COL1 = '$title'")
        db.close()
    }

    fun getGoal(goalId: Int): Cursor {
        val db = this.writableDatabase
        return db.rawQuery("SELECT * FROM $GOAL_TABLE_NAME WHERE ID = $goalId", null)

    }

    fun getAllCheckpointsOfGoal(goalId: Int): Cursor{
        val db = this.writableDatabase
        val c = db.rawQuery("SELECT * FROM $CHECKPOINT_TABLE_NAME WHERE $CHECKPOINT_COL2 = $goalId", null)
        return c
    }

    companion object {
        val GOAL_TABLE_NAME = "Goals"
        val GOAL_COL1 = "Name"
        val GOAL_COL2 = "Percentage"
        val GOAL_COL3 = "Date"
        val GOAL_COL4 = "Finished"
        var GOAL_COUNT = 0

        val CHECKPOINT_TABLE_NAME = "Checkpoints"
        val CHECKPOINT_COL1 = "Name"
        val CHECKPOINT_COL2 = "GoalId"
        val CHECKPOINT_COL3 = "Date"
        val CHECKPOINT_COL4 = "Completed"
    }

    
}
