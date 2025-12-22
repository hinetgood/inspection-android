package com.inspection.app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "addresses",
    foreignKeys = [
        ForeignKey(
            entity = Case::class,
            parentColumns = ["id"],
            childColumns = ["caseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("caseId")]
)
data class Address(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val caseId: Long,
    val address: String,
    // 建物資訊 (JSON 字串)
    val structure: String = "",        // 構造
    val structureOther: String = "",
    val usage: String = "",            // 用途
    val usageOther: String = "",
    val wall: String = "",             // 牆面
    val wallOther: String = "",
    val ceiling: String = "",          // 平頂
    val ceilingOther: String = "",
    val floor: String = "",            // 地坪
    val floorOther: String = "",
    val surveyStatus: String = "",     // 會勘狀況
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
