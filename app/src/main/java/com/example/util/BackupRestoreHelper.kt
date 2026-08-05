package com.example.util

import android.content.Context
import com.example.data.AppDatabase
import com.example.data.model.Customer
import com.example.data.model.PaymentRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object BackupRestoreHelper {

    suspend fun createBackup(context: Context): File? = withContext(Dispatchers.IO) {
        try {
            val db = AppDatabase.getDatabase(context)
            val customers = db.customerDao().getAllCustomersList()
            val payments = db.paymentRecordDao().getAllPaymentRecordsList()

            val jsonRoot = JSONObject()
            jsonRoot.put("version", 1)
            jsonRoot.put("appName", "Dolphin Fiber Network")
            jsonRoot.put("backupDate", System.currentTimeMillis())

            val customersArr = JSONArray()
            for (c in customers) {
                val obj = JSONObject().apply {
                    put("id", c.id)
                    put("customerCode", c.customerCode)
                    put("photoUri", c.photoUri ?: "")
                    put("name", c.name)
                    put("fatherName", c.fatherName)
                    put("phoneNumber", c.phoneNumber)
                    put("whatsappNumber", c.whatsappNumber)
                    put("address", c.address)
                    put("area", c.area)
                    put("gpsLocation", c.gpsLocation)
                    put("internetPackage", c.internetPackage)
                    put("monthlyFee", c.monthlyFee)
                    put("dueDateDay", c.dueDateDay)
                    put("isPaidThisMonth", c.isPaidThisMonth)
                    put("lastPaidDate", c.lastPaidDate ?: "")
                    put("installationDate", c.installationDate)
                    put("notes", c.notes)
                }
                customersArr.put(obj)
            }
            jsonRoot.put("customers", customersArr)

            val paymentsArr = JSONArray()
            for (p in payments) {
                val obj = JSONObject().apply {
                    put("id", p.id)
                    put("customerId", p.customerId)
                    put("customerName", p.customerName)
                    put("amount", p.amount)
                    put("paymentDate", p.paymentDate)
                    put("monthYear", p.monthYear)
                    put("notes", p.notes)
                }
                paymentsArr.put(obj)
            }
            jsonRoot.put("payments", paymentsArr)

            val backupDir = context.getExternalFilesDir(null) ?: context.filesDir
            val backupFile = File(backupDir, "Dolphin_Fiber_Backup_${System.currentTimeMillis()}.json")

            FileOutputStream(backupFile).use { out ->
                out.write(jsonRoot.toString(2).toByteArray())
            }

            backupFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun restoreBackup(context: Context, backupFile: File): Boolean = withContext(Dispatchers.IO) {
        try {
            val content = FileInputStream(backupFile).bufferedReader().use { it.readText() }
            val jsonRoot = JSONObject(content)

            val db = AppDatabase.getDatabase(context)

            if (jsonRoot.has("customers")) {
                val custArr = jsonRoot.getJSONArray("customers")
                val restoredCustomers = mutableListOf<Customer>()
                for (i in 0 until custArr.length()) {
                    val obj = custArr.getJSONObject(i)
                    restoredCustomers.add(
                        Customer(
                            id = obj.optLong("id", 0),
                            customerCode = obj.optString("customerCode", ""),
                            photoUri = obj.optString("photoUri").ifEmpty { null },
                            name = obj.optString("name", "Customer"),
                            fatherName = obj.optString("fatherName", ""),
                            phoneNumber = obj.optString("phoneNumber", ""),
                            whatsappNumber = obj.optString("whatsappNumber", ""),
                            address = obj.optString("address", ""),
                            area = obj.optString("area", ""),
                            gpsLocation = obj.optString("gpsLocation", ""),
                            internetPackage = obj.optString("internetPackage", "20 Mbps Fiber"),
                            monthlyFee = obj.optDouble("monthlyFee", 2500.0),
                            dueDateDay = obj.optInt("dueDateDay", 1),
                            isPaidThisMonth = obj.optBoolean("isPaidThisMonth", false),
                            lastPaidDate = obj.optString("lastPaidDate").ifEmpty { null },
                            installationDate = obj.optString("installationDate", "2026-01-01"),
                            notes = obj.optString("notes", "")
                        )
                    )
                }
                if (restoredCustomers.isNotEmpty()) {
                    db.customerDao().deleteAll()
                    db.customerDao().insertAll(restoredCustomers)
                }
            }

            if (jsonRoot.has("payments")) {
                val payArr = jsonRoot.getJSONArray("payments")
                val restoredPayments = mutableListOf<PaymentRecord>()
                for (i in 0 until payArr.length()) {
                    val obj = payArr.getJSONObject(i)
                    restoredPayments.add(
                        PaymentRecord(
                            id = obj.optLong("id", 0),
                            customerId = obj.optLong("customerId", 0),
                            customerName = obj.optString("customerName", ""),
                            amount = obj.optDouble("amount", 0.0),
                            paymentDate = obj.optString("paymentDate", ""),
                            monthYear = obj.optString("monthYear", ""),
                            notes = obj.optString("notes", "")
                        )
                    )
                }
                if (restoredPayments.isNotEmpty()) {
                    db.paymentRecordDao().deleteAll()
                    db.paymentRecordDao().insertAll(restoredPayments)
                }
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
