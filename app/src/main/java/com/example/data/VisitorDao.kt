package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VisitorDao {
    @Query("SELECT * FROM visitor_records ORDER BY createdAt DESC")
    fun getAllVisitors(): Flow<List<VisitorRecord>>

    @Query("SELECT * FROM visitor_records WHERE id = :id LIMIT 1")
    suspend fun getVisitorById(id: Int): VisitorRecord?

    @Query("SELECT * FROM visitor_records WHERE otpCode = :otp LIMIT 1")
    suspend fun getVisitorByOtp(otp: String): VisitorRecord?

    @Query("SELECT * FROM visitor_records WHERE hostName = :hostName ORDER BY createdAt DESC")
    fun getVisitorsByHost(hostName: String): Flow<List<VisitorRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisitor(record: VisitorRecord): Long

    @Update
    suspend fun updateVisitor(record: VisitorRecord)

    @Delete
    suspend fun deleteVisitor(record: VisitorRecord)

    @Query("DELETE FROM visitor_records")
    suspend fun deleteAllVisitors()
}
