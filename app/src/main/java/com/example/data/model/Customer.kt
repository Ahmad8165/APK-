package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerCode: String = "",
    val photoUri: String? = null,
    val name: String,
    val fatherName: String = "",
    val phoneNumber: String,
    val whatsappNumber: String = "",
    val address: String = "",
    val area: String = "",
    val gpsLocation: String = "", // e.g. "31.5204, 74.3587"
    val internetPackage: String = "20 Mbps Fiber",
    val monthlyFee: Double = 2500.0,
    val dueDateDay: Int = 5, // Day of month (1-31)
    val isPaidThisMonth: Boolean = false,
    val lastPaidDate: String? = null, // e.g. "2026-08-01"
    val installationDate: String = "2026-01-01",
    val notes: String = "",
    val isSuspended: Boolean = false
)
