package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payment_records")
data class PaymentRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: Long,
    val customerName: String = "",
    val customerCode: String = "",
    val internetPackage: String = "",
    val amount: Double = 0.0,
    val paymentDate: String = "", // e.g. "2026-08-02"
    val monthYear: String = "",  // e.g. "August 2026"
    val paymentMethod: String = "Cash", // Cash / Online / Bank Transfer
    val collectedBy: String = "Admin",
    val paymentStatus: String = "Paid",
    val receiptNumber: String = "", // e.g. "RCP-202608-0001"
    val nextDueDate: String = "",
    val notes: String = "Monthly Subscription Fee"
)

