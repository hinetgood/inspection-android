package com.inspection.app.data.dao

import androidx.room.*
import com.inspection.app.data.entity.Photo
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {
    @Query("SELECT * FROM photos WHERE addressId = :addressId ORDER BY sequence ASC")
    fun getPhotosByAddress(addressId: Long): Flow<List<Photo>>

    @Query("SELECT * FROM photos WHERE addressId = :addressId ORDER BY sequence ASC")
    suspend fun getPhotosByAddressList(addressId: Long): List<Photo>

    @Query("SELECT * FROM photos WHERE addressId = :addressId ORDER BY sequence ASC")
    suspend fun getPhotosByAddressOnce(addressId: Long): List<Photo>

    @Query("SELECT * FROM photos WHERE id = :id")
    suspend fun getPhotoById(id: Long): Photo?

    @Query("SELECT COALESCE(MAX(sequence), 0) + 1 FROM photos WHERE addressId = :addressId")
    suspend fun getNextSequence(addressId: Long): Int

    @Insert
    suspend fun insert(photo: Photo): Long

    @Update
    suspend fun update(photo: Photo)

    @Delete
    suspend fun delete(photo: Photo)

    @Query("DELETE FROM photos WHERE id = :id")
    suspend fun deleteById(id: Long)

    // 重新排序照片序號
    @Query("UPDATE photos SET sequence = :newSequence WHERE id = :id")
    suspend fun updateSequence(id: Long, newSequence: Int)
}
