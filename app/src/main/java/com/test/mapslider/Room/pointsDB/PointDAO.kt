package com.test.mapslider.Room.pointsDB

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

/**
 * @Author: Shubham Rimjha
 * @Date: 06-11-2021
 */
@Dao
interface PointDAO {
    @Insert
    fun insertPoint(pointEntity: PointEntity)

    @Delete
    fun deletePoint(pointEntity: PointEntity)

    @Query("SELECT * FROM points")
    fun getAllPoints(): List<PointEntity>
}