package com.test.mapslider.Room.pointsDB

import android.app.Activity
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * @Author: Shubham Rimjha
 * @Date: 06-11-2021
 */
class DBHelper : SQLiteOpenHelper(Activity() as Context, "database", null, 0) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(
            "CREATE TABLE tableMap(" +
                    "pointID INT PRIMARY KEY," +
                    "bookName DOUBLE," +
                    "bookAuthor DOUBLE," +
                    ")"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }
}