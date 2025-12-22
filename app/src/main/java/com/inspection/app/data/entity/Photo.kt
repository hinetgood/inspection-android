package com.inspection.app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "photos",
    foreignKeys = [
        ForeignKey(
            entity = Address::class,
            parentColumns = ["id"],
            childColumns = ["addressId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("addressId")]
)
data class Photo(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val addressId: Long,
    val sequence: Int,                 // 照片序號
    val originalPath: String,          // 原始照片路徑
    val watermarkedPath: String = "",  // 浮水印照片路徑
    val thumbnailPath: String = "",    // 縮圖路徑
    val position: String = "牆",       // 位置：全景/牆/平頂/地坪/樑/柱/其他
    val material: String = "P",        // 材料代碼
    val crackWidth: String = "",       // 裂縫寬度
    val crackShape: String = "",       // 裂縫形狀
    val crackCount: String = "",       // 裂縫數量
    val peeling: Boolean = false,      // 剝落
    val seepage: Boolean = false,      // 滲漬
    val remark: String = "",           // 備註
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
