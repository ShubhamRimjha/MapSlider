package com.test.mapslider.Room.pointsDB

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @Author: Shubham Rimjha
 * @Date: 06-11-2021
 */
@Entity(tableName = "points")
class PointEntity(
    @PrimaryKey val point_id: Int,
    @ColumnInfo(name = "lat") val lat: Double,
    @ColumnInfo(name = "lng") val lng: Double
)