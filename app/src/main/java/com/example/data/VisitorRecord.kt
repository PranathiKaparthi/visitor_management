package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "visitor_records")
data class VisitorRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val visitorId: String, // e.g. VMS-2026-0001
    val name: String,
    val phone: String,
    val email: String,
    val company: String,
    val designation: String,
    val hostName: String,
    val department: String,
    val floor: String,
    val meetingRoom: String,
    val purpose: String,
    val checkInTime: Long, // 0 if not checked-in yet
    val expectedCheckOutTime: Long,
    val actualCheckOutTime: Long, // 0 if not checked-out yet
    val status: String, // "PENDING", "APPROVED", "REJECTED", "CHECKED_IN", "CHECKED_OUT"
    val photoUri: String = "", // Placeholders or captured image files
    val idProofUri: String = "",
    val vehicleNumber: String = "",
    val otpCode: String, // 6-digit numeric string for security verification
    val createdAt: Long = System.currentTimeMillis()
) : Serializable
