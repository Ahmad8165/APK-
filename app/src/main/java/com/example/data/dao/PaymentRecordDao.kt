package com.example.data.dao

import androidx.room.*
import com.example.data.model.PaymentRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentRecordDao {
    @Query("SELECT * FROM payment_records ORDER BY paymentDate DESC, id DESC")
    fun getAllPaymentRecords(): Flow<List<PaymentRecord>>

    @Query("SELECT * FROM payment_records ORDER BY paymentDate DESC, id DESC")
    suspend fun getAllPaymentRecordsList(): List<PaymentRecord>

    @Query("SELECT * FROM payment_records WHERE customerId = :customerId ORDER BY paymentDate DESC")
    fun getPaymentsForCustomer(customerId: Long): Flow<List<PaymentRecord>>

    @Query("SELECT * FROM payment_records WHERE customerId = :customerId ORDER BY paymentDate DESC")
    suspend fun getPaymentsListForCustomer(customerId: Long): List<PaymentRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(record: PaymentRecord): Long

    @Delete
    suspend fun deletePayment(record: PaymentRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<PaymentRecord>)

    @Query("DELETE FROM payment_records")
    suspend fun deleteAll()
}
