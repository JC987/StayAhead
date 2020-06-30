package com.example.stayahead

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log


class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, GOAL_TABLE_NAME, null, 1) {
    init {
        Log.d("TAG", "DatabaseHelper: NEW DB HELPER")

    }

    override fun onCreate(sqLiteDatabase: SQLiteDatabase) {
        val createGoalTable = "CREATE TABLE " + GOAL_TABLE_NAME + "( ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                GOAL_COL1 + " TEXT, " +
                GOAL_COL2 + " TEXT, " +
                GOAL_COL3 + " DATE Default CURRENT_DATE, " +
                GOAL_COL4 + " Time Default CURRENT_TIME, " +
                GOAL_COL5 + " BOOLEAN)"

        Log.d("TAG", "onCreate: table created")
        val createCheckpointTable = "CREATE TABLE $CHECKPOINT_TABLE_NAME ( ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$CHECKPOINT_COL1 TEXT, " +
                "$CHECKPOINT_COL2 INT, " +
                "$CHECKPOINT_COL3 DATE Default CURRENT_DATE, " +
                "$CHECKPOINT_COL4 TIME Default CURRENT_TIME, " +
                "$CHECKPOINT_COL5 BOOLEAN, "  +
                "FOREIGN KEY($CHECKPOINT_COL2) REFERENCES $GOAL_TABLE_NAME(ID)" + ")"

        Log.d("TAG", "onCreate: table created")

        sqLiteDatabase.execSQL(createGoalTable)

        sqLiteDatabase.execSQL(createCheckpointTable)
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, i: Int, i1: Int) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS $GOAL_TABLE_NAME")
        onCreate(sqLiteDatabase)
    }


    fun getGoal(goalId: Int): Cursor {
        val db = this.writableDatabase
        return db.rawQuery("SELECT * FROM $GOAL_TABLE_NAME WHERE ID = $goalId", null)
    }

    fun addGoalData(goal: Goal): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(GOAL_COL1, goal.goalName)
        contentValues.put(GOAL_COL2, goal.remainingPercentage)
        if(goal.date != "")
            contentValues.put(GOAL_COL3, goal.date)
        if(goal.time != "")
            contentValues.put(GOAL_COL4, goal.time)
        contentValues.put(GOAL_COL5, goal.isFinished)
        val result = db.insert(GOAL_TABLE_NAME, null, contentValues)
        val i: Int = -1
        Log.d("TAG", "added called")
        GOAL_COUNT++
        return result != i.toLong()
    }

    fun getGoalDBCount(): Int{
        val db = this.writableDatabase
        val c = db.rawQuery("SELECT seq FROM SQLITE_SEQUENCE WHERE name = '$GOAL_TABLE_NAME'",null)
        c.moveToFirst()
            //var count = 0
        if(c.count == 0)
            return 0
        return c.getInt(0)
    }

    fun getActiveGoalsData(desc: Boolean): Cursor {
        val db = this.writableDatabase
        return if (desc) db.rawQuery(
            "SELECT * FROM $GOAL_TABLE_NAME WHERE $GOAL_COL5 = 0 ORDER BY id DESC",
            null
        ) else db.rawQuery("SELECT * FROM $GOAL_TABLE_NAME WHERE $GOAL_COL5 = 0" , null)

    }

    fun getActiveGoalsDataByDate(): Cursor {
        val db = this.writableDatabase
        return db.rawQuery("SELECT * FROM $GOAL_TABLE_NAME WHERE $GOAL_COL5 = 0 ORDER BY $GOAL_COL3 DESC, id DESC" , null)
    }

    fun getAllCheckpointData(desc: Boolean): Cursor {
        val db = this.writableDatabase
        return if (desc) db.rawQuery(
            "SELECT * FROM $CHECKPOINT_TABLE_NAME ORDER BY id DESC",
            null
        ) else db.rawQuery("SELECT * FROM $CHECKPOINT_TABLE_NAME", null)

    }


    fun getCheckpointDBCount(): Int{
        val db = this.writableDatabase
        val c = db.rawQuery("SELECT seq FROM SQLITE_SEQUENCE WHERE name = '$CHECKPOINT_TABLE_NAME'",null)
        c.moveToFirst()
        //var count = 0
        if(c.count == 0)
            return 0
        return c.getInt(0)
    }

    fun getAllCheckpointsOfGoal(goalId: Int): Cursor{
        val db = this.writableDatabase
        return db.rawQuery("SELECT * FROM $CHECKPOINT_TABLE_NAME WHERE $CHECKPOINT_COL2 = $goalId", null)
    }

    fun getNumberOFCheckpointsCompleted(): Int{
        val db = this.writableDatabase
        val c = db.rawQuery("SELECT COUNT(id) FROM $CHECKPOINT_TABLE_NAME WHERE $CHECKPOINT_COL5 = 1",null)
        c.moveToFirst()
        return c.getInt(0)
    }
    fun getNumberOFCheckpointsFailed(): Int{
        val db = this.writableDatabase
        val c = db.rawQuery("SELECT COUNT(id) FROM $CHECKPOINT_TABLE_NAME WHERE $CHECKPOINT_COL5 = 0",null)
        c.moveToFirst()
        return c.getInt(0)
    }

    fun addCheckpointData(checkpoint: Checkpoint): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        val tmp = checkpoint.checkpointName

        Log.d("TAG", ";;; " + checkpoint.checkpointName)
        contentValues.put(CHECKPOINT_COL1, tmp)
        contentValues.put(CHECKPOINT_COL2, checkpoint.goalId)
        if(checkpoint.date != "Date")
            contentValues.put(CHECKPOINT_COL3, checkpoint.date)
        if(checkpoint.time != "Time")
            contentValues.put(CHECKPOINT_COL4, checkpoint.time)
        contentValues.put(CHECKPOINT_COL5, checkpoint.isCompleted)
        val result = db.insert(CHECKPOINT_TABLE_NAME, null, contentValues)
        val i: Int = -1
        //db.close()
        Log.d("TAG", "added called")
        return result != i.toLong()
    }

    fun updateCheckpointData(updatedCheckpoint: Checkpoint){
        val db = this.writableDatabase
        Log.d("TAG","col will be " + updatedCheckpoint.checkpointName)
        val contentValues = ContentValues()
        contentValues.put(CHECKPOINT_COL1,updatedCheckpoint.checkpointName)
        contentValues.put(CHECKPOINT_COL3,updatedCheckpoint.date)
        contentValues.put(CHECKPOINT_COL4,updatedCheckpoint.time)
        //TODO: UPDATE newTime
        val where = "ID = ?"
        db.update(CHECKPOINT_TABLE_NAME,contentValues,where,arrayOf(updatedCheckpoint.checkpointId.toString()))
        //db.execSQL("UPDATE $CHECKPOINT_TABLE_NAME SET $CHECKPOINT_COL1 = '${newName}', $CHECKPOINT_COL3 = '${newDate}' WHERE ID = ${checkpointId}")
    }

    fun updateCheckpointCompleted(id:Int, value:Int){
        val db = this.writableDatabase
        db.execSQL("UPDATE $CHECKPOINT_TABLE_NAME SET $CHECKPOINT_COL5 = $value WHERE ID = $id")
    }

    fun updateGoalNameAndDate(updatedGoal: Goal){
        val db = this.writableDatabase
        val contentValue = ContentValues()
        contentValue.put(GOAL_COL1,updatedGoal.goalName)
        contentValue.put(GOAL_COL2,updatedGoal.remainingPercentage)
        contentValue.put(GOAL_COL3,updatedGoal.date)
        contentValue.put(GOAL_COL4,updatedGoal.time)
        val where = "ID = ?"
        val args = ArrayList<String?>()
        args.add(updatedGoal.goalId.toString())
        db.update(GOAL_TABLE_NAME,contentValue,where, arrayOf(updatedGoal.goalId.toString()))
        //db.execSQL("UPDATE $GOAL_TABLE_NAME SET $GOAL_COL1 = '${newGoalName}', $GOAL_COL3 = '${newGoalDate}', $GOAL_COL2 = '${newPercent}' WHERE ID = ${goalId}")

    }

    fun updateGoalPercentage(id:Int, per:String){
        val db = this.writableDatabase
        val contentValue = ContentValues()
        contentValue.put(GOAL_COL2,per)
        val where = "ID = ?"
        db.update(GOAL_TABLE_NAME,contentValue,where, arrayOf(id.toString()))
        //db.execSQL("UPDATE $GOAL_TABLE_NAME SET $GOAL_COL2 = $per WHERE ID = $id")
    }

    fun finishGoal(goalId:Int){
        val db = this.writableDatabase
        db.execSQL("UPDATE $GOAL_TABLE_NAME SET $GOAL_COL5 = 1 WHERE ID = $goalId")
    }

    fun getFinishedGoals(desc:Boolean): Cursor{
        val db = this.writableDatabase
        return if(desc)
            db.rawQuery("SELECT * FROM $GOAL_TABLE_NAME  WHERE $GOAL_COL5 = 1 ORDER BY id DESC", null)
        else
            db.rawQuery("SELECT * FROM $GOAL_TABLE_NAME  WHERE $GOAL_COL5 = 1 ORDER BY id ASC", null)

    }

    fun getFinishedGoalsDataByDate(): Cursor {
        val db = this.writableDatabase
        return db.rawQuery("SELECT * FROM $GOAL_TABLE_NAME WHERE $GOAL_COL5 = 1 ORDER BY $GOAL_COL3, id DESC" , null)
    }

    fun deleteGoal(goalId:Int){
        val db = this.writableDatabase

        db.execSQL("DELETE FROM $CHECKPOINT_TABLE_NAME WHERE $CHECKPOINT_COL2 = $goalId")
        db.execSQL("DELETE FROM $GOAL_TABLE_NAME WHERE ID = $goalId")
        //db.close()
    }


    fun truncateTables() {
        val db = this.writableDatabase
        //no truncate tables in sqlite
        db.execSQL("DELETE FROM $GOAL_TABLE_NAME")
        db.execSQL("DELETE FROM SQLITE_SEQUENCE")
        db.execSQL("DELETE FROM $CHECKPOINT_TABLE_NAME")
        db.execSQL("VACUUM")
        Log.d("TAG", "truncate tables")
    }

    private companion object {
        const val GOAL_TABLE_NAME = "Goals"
        const val GOAL_COL1 = "Name"
        const val GOAL_COL2 = "Percentage"
        const val GOAL_COL3 = "Date"
        const val GOAL_COL4 = "Time"
        const val GOAL_COL5 = "Finished"
        var GOAL_COUNT = 0

        const val CHECKPOINT_TABLE_NAME = "Checkpoints"
        const val CHECKPOINT_COL1 = "Name"
        const val CHECKPOINT_COL2 = "GoalId"
        const val CHECKPOINT_COL3 = "Date"
        const val CHECKPOINT_COL4 = "Time"
        const val CHECKPOINT_COL5 = "Completed"
    }

    
}
