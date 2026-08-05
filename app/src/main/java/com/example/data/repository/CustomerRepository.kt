package com.example.data.repository

import com.example.data.dao.CustomerDao
import com.example.data.dao.PaymentRecordDao
import com.example.data.model.Customer
import com.example.data.model.PaymentRecord
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CustomerRepository(
    private val customerDao: CustomerDao,
    private val paymentRecordDao: PaymentRecordDao
) {
    val allCustomers: Flow<List<Customer>> = customerDao.getAllCustomers()
    val allPaymentRecords: Flow<List<PaymentRecord>> = paymentRecordDao.getAllPaymentRecords()

    fun getPaymentsForCustomer(customerId: Long): Flow<List<PaymentRecord>> {
        return paymentRecordDao.getPaymentsForCustomer(customerId)
    }

    suspend fun addCustomer(customer: Customer): Long {
        var codeToUse = customer.customerCode.trim()
        if (codeToUse.isEmpty()) {
            val all = customerDao.getAllCustomersList()
            val highestNum = all.mapNotNull { c ->
                val numPart = c.customerCode.removePrefix("DF-").removePrefix("DFN-").toIntOrNull()
                numPart
            }.maxOrNull() ?: all.size
            codeToUse = "DF-%04d".format(highestNum + 1)
        }
        val customerWithCode = customer.copy(customerCode = codeToUse)
        val id = customerDao.insertCustomer(customerWithCode)
        return id
    }

    suspend fun updateCustomer(customer: Customer) {
        var codeToUse = customer.customerCode.trim()
        if (codeToUse.isEmpty()) {
            codeToUse = "DF-%04d".format(customer.id)
        }
        customerDao.updateCustomer(customer.copy(customerCode = codeToUse))
    }

    suspend fun deleteCustomer(customer: Customer) {
        customerDao.deleteCustomer(customer)
    }

    suspend fun markAsPaid(
        customer: Customer,
        paymentMethod: String = "Cash",
        collectedBy: String = "Admin",
        customAmount: Double? = null,
        notes: String? = null
    ): PaymentRecord {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val monthYearStr = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())

        // Calculate next due date
        val nextDueCal = Calendar.getInstance()
        nextDueCal.add(Calendar.MONTH, 1)
        val nextDueDay = customer.dueDateDay.coerceAtMost(nextDueCal.getActualMaximum(Calendar.DAY_OF_MONTH))
        nextDueCal.set(Calendar.DAY_OF_MONTH, nextDueDay)
        val nextDueDateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(nextDueCal.time)

        val updatedCustomer = customer.copy(
            isPaidThisMonth = true,
            lastPaidDate = todayStr
        )
        customerDao.updateCustomer(updatedCustomer)

        val code = if (customer.customerCode.isNotBlank()) customer.customerCode else "DF-%04d".format(customer.id)
        val amountToPay = customAmount ?: customer.monthlyFee
        val rcpNum = "RCP-%s-%04d".format(
            SimpleDateFormat("yyyyMM", Locale.getDefault()).format(Date()),
            (1000..9999).random()
        )

        val paymentRecord = PaymentRecord(
            customerId = customer.id,
            customerName = customer.name,
            customerCode = code,
            internetPackage = customer.internetPackage,
            amount = amountToPay,
            paymentDate = todayStr,
            monthYear = monthYearStr,
            paymentMethod = paymentMethod,
            collectedBy = collectedBy,
            paymentStatus = "Paid",
            receiptNumber = rcpNum,
            nextDueDate = nextDueDateStr,
            notes = notes ?: "Monthly Internet Fee (${customer.internetPackage})"
        )
        val recordId = paymentRecordDao.insertPayment(paymentRecord)
        return paymentRecord.copy(id = recordId)
    }

    suspend fun markAsUnpaid(customer: Customer) {
        val updatedCustomer = customer.copy(
            isPaidThisMonth = false
        )
        customerDao.updateCustomer(updatedCustomer)
    }

    suspend fun prepopulateIfEmpty() {
        if (customerDao.getCustomerCount() == 0) {
            val cal = Calendar.getInstance()
            val todayDay = cal.get(Calendar.DAY_OF_MONTH)
            val tomorrowDay = if (todayDay >= 28) 1 else todayDay + 1
            val nextWeekDay = if (todayDay >= 22) 2 else todayDay + 6

            val sampleCustomers = listOf(
                Customer(
                    customerCode = "DF-0001",
                    name = "Tariq Mahmood",
                    fatherName = "Mahmood Ahmed",
                    phoneNumber = "+92 300 1234567",
                    whatsappNumber = "+92 300 1234567",
                    address = "House 45, Street 12, Sector A",
                    area = "Gulberg Greens",
                    gpsLocation = "31.5204, 74.3587",
                    internetPackage = "20 Mbps Fiber",
                    monthlyFee = 2500.0,
                    dueDateDay = todayDay,
                    isPaidThisMonth = false,
                    installationDate = "2025-06-15",
                    notes = "VIP Customer, prefers evening collection"
                ),
                Customer(
                    customerCode = "DF-0002",
                    name = "Usman Ali",
                    fatherName = "Muhammad Ali",
                    phoneNumber = "+92 321 9876543",
                    whatsappNumber = "+92 321 9876543",
                    address = "Plot 18-B, Block 4",
                    area = "Model Town",
                    gpsLocation = "31.4800, 74.3200",
                    internetPackage = "50 Mbps Fiber Ultra",
                    monthlyFee = 4500.0,
                    dueDateDay = tomorrowDay,
                    isPaidThisMonth = false,
                    installationDate = "2025-08-01",
                    notes = "Online banking transfer"
                ),
                Customer(
                    customerCode = "DF-0003",
                    name = "Bilal Hassan",
                    fatherName = "Hassan Raza",
                    phoneNumber = "+92 333 5551234",
                    whatsappNumber = "+92 333 5551234",
                    address = "Villa 89, Phase 5",
                    area = "DHA Phase 5",
                    gpsLocation = "31.4690, 74.3800",
                    internetPackage = "30 Mbps Fiber",
                    monthlyFee = 3200.0,
                    dueDateDay = todayDay,
                    isPaidThisMonth = true,
                    lastPaidDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                    installationDate = "2025-02-10",
                    notes = "Paid via cash on 1st"
                ),
                Customer(
                    customerCode = "DF-0004",
                    name = "Kamran Shah",
                    fatherName = "Syed Shah",
                    phoneNumber = "+92 312 4448899",
                    whatsappNumber = "+92 312 4448899",
                    address = "Shop 12, Main Market",
                    area = "Gulberg Greens",
                    gpsLocation = "31.5100, 74.3500",
                    internetPackage = "100 Mbps Corporate",
                    monthlyFee = 8500.0,
                    dueDateDay = nextWeekDay,
                    isPaidThisMonth = false,
                    installationDate = "2024-11-20",
                    notes = "Commercial Connection"
                ),
                Customer(
                    customerCode = "DF-0005",
                    name = "Zubair Khan",
                    fatherName = "Farooq Khan",
                    phoneNumber = "+92 345 6677889",
                    whatsappNumber = "+92 345 6677889",
                    address = "Flat 302, Royal Residency",
                    area = "Johar Town",
                    gpsLocation = "31.4700, 74.2800",
                    internetPackage = "20 Mbps Fiber",
                    monthlyFee = 2500.0,
                    dueDateDay = nextWeekDay,
                    isPaidThisMonth = true,
                    lastPaidDate = "2026-08-01",
                    installationDate = "2025-04-05",
                    notes = "Auto-reminder requested"
                )
            )

            for (customer in sampleCustomers) {
                val id = customerDao.insertCustomer(customer)
                if (customer.isPaidThisMonth) {
                    paymentRecordDao.insertPayment(
                        PaymentRecord(
                            customerId = id,
                            customerName = customer.name,
                            amount = customer.monthlyFee,
                            paymentDate = customer.lastPaidDate ?: "2026-08-01",
                            monthYear = "August 2026",
                            notes = "Monthly Subscription Fee (${customer.internetPackage})"
                        )
                    )
                }
            }
        }
    }

    suspend fun exportBackupJson(): String {
        // Simple structured JSON export
        val customers = customerDao.getAllCustomers()
        // We'll collect list or query directly
        return ""
    }
}
