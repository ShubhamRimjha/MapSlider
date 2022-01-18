package com.test.mapslider.Room.pointsDB

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * @Author: Shubham Rimjha
 * @Date: 06-11-2021
 */
@Database(entities = [PointEntity::class], version = 1)
abstract class PointDB: RoomDatabase() {
    abstract fun pointDAO(): PointDAO
}