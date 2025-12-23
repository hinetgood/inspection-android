package com.inspection.app.data.dao

import androidx.room.*
import com.inspection.app.data.entity.Case
import kotlinx.coroutines.flow.Flow

@Dao
interface CaseDao {
    @Query("SELECT * FROM cases ORDER BY createdAt DESC")
    fun getAllCases(): Flow<List<Case>>

    @Query("SELECT * FROM cases WHERE id = :id")
    suspend fun getCaseById(id: Long): Case?

    @Insert
    suspend fun insert(caseEntity: Case): Long

    @Update
    suspend fun update(caseEntity: Case)

    @Delete
    suspend fun delete(caseEntity: Case)

    @Query("DELETE FROM cases WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM addresses WHERE caseId = :caseId")
    suspend fun getAddressCount(caseId: Long): Int

    @Query("SELECT COUNT(*) FROM photos WHERE addressId IN (SELECT id FROM addresses WHERE caseId = :caseId)")
    suspend fun getPhotoCount(caseId: Long): Int
}
