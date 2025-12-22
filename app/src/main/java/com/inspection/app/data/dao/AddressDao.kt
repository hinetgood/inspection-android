package com.inspection.app.data.dao

import androidx.room.*
import com.inspection.app.data.entity.Address
import kotlinx.coroutines.flow.Flow

@Dao
interface AddressDao {
    @Query("SELECT * FROM addresses WHERE caseId = :caseId ORDER BY createdAt DESC")
    fun getAddressesByCase(caseId: Long): Flow<List<Address>>

    @Query("SELECT * FROM addresses WHERE id = :id")
    suspend fun getAddressById(id: Long): Address?

    @Insert
    suspend fun insert(address: Address): Long

    @Update
    suspend fun update(address: Address)

    @Delete
    suspend fun delete(address: Address)

    @Query("DELETE FROM addresses WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM photos WHERE addressId = :addressId")
    suspend fun getPhotoCount(addressId: Long): Int
}
