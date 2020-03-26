package com.example.stayahead

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, TABLE_NAME, null, 1) {
    init {
        Log.d("TAG", "DatabaseHelper: NEW DB HELPER")
    }

    override fun onCreate(sqLiteDatabase: SQLiteDatabase) {
        val createTable = "CREATE TABLE " + TABLE_NAME + "( ID INTEGER PRIMARY KEY " +
                "AUTOINCREMENT, " + COL1 + " TEXT, " + COL2 + " TEXT, " + COL3 + " DATE Default CURRENT_DATE, " + COL4 + " BOOLEAN)"
        Log.d("TAG", "onCreate: table created")

        sqLiteDatabase.execSQL(createTable)
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, i: Int, i1: Int) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(sqLiteDatabase)
    }

    fun getInfo():Cursor{

        val otherTmp = "SELECT name FROM sqlite_master WHERE type='table' AND name=$TABLE_NAME"
        val db = this.writableDatabase
        return db.rawQuery(otherTmp,null)

    }

    fun addData(goal: Goal): Boolean {
        val db = this.writableDatabase
        // db.rawQuery("CREATE TABLE " + TABLE_NAME, null);
        val contentValues = ContentValues()
        contentValues.put(COL1, goal.goalName)
        contentValues.put(COL2, goal.remainingPercentage)
        contentValues.put(COL4, goal.isFinished)
        val result = db.insert(TABLE_NAME, null, contentValues)
        val i: Int = -1
        Log.d("TAG", "added called")
        return result != i.toLong()
    }

    /**
     * Returns all data
     * @return
     */
    fun getData(desc: Boolean): Cursor {

        val db = this.writableDatabase


        //db.execSQL("CREATE TABLE IF NOT EXISTS "+ TABLE_NAME);
        return if (desc) db.rawQuery(
            "SELECT * FROM $TABLE_NAME ORDER BY id DESC",
            null
        ) else db.rawQuery("SELECT * FROM $TABLE_NAME", null)

    }

    /**
     * Returns all data
     * @return
     */
    fun deleteDB() {
        val db = this.writableDatabase
        db.execSQL("DELETE FROM  $TABLE_NAME")

       // db.execSQL("DELETE FROM goals")
     //   db.execSQL("DELETE FROM HolyGoals")

    }

    fun deleteItem(title: String) {
        val db = this.writableDatabase
        db.execSQL("DELETE FROM  $TABLE_NAME WHERE $COL1 = '$title'")
    }

    companion object {
        val TABLE_NAME = "Goals"
        val COL1 = "name"
        val COL2 = "percentage"
        val COL3 = "date"
        val COL4 = "finished"
    }

}
