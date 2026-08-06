package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.MainActivity
import com.example.data.AppDatabase
import com.example.data.model.Customer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.concurrent.TimeUnit

class DuePaymentWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val db = AppDatabase.getDatabase(context)
            val calendar = Calendar.getInstance()
            val todayDay = calendar.get(Calendar.DAY_OF_MONTH)

            val dueCustomers = db.customerDao()
                .getCustomersDueOnDay(todayDay)
                .filter { !it.isPaidThisMonth }

            if (dueCustomers.isNotEmpty()) {
                sendDueCustomersNotification(context, dueCustomers)
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    private fun sendDueCustomersNotification(context: Context, dueCustomers: List<Customer>) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Daily Due Fee Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Periodic alerts for customers whose internet subscription fees are due today."
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val totalAmount = dueCustomers.sumOf { it.monthlyFee }.toInt()
        val title = if (dueCustomers.size == 1) {
            "🐬 Dolphin Fiber: 1 Fee Due Today"
        } else {
            "🐬 Dolphin Fiber: ${dueCustomers.size} Fees Due Today"
        }

        val shortMessage = "${dueCustomers.size} customer(s) pending today. Total: PKR $totalAmount"

        val customerListText = dueCustomers.take(5).joinToString("\n") { customer ->
            val code = customer.customerCode.ifBlank { "ID-${customer.id}" }
            "• ${customer.name} ($code) - PKR ${customer.monthlyFee.toInt()} [${customer.area.ifBlank { customer.internetPackage }}]"
        }
        val moreText = if (dueCustomers.size > 5) "\n...and ${dueCustomers.size - 5} more customer(s)" else ""
        val expandedBigText = "Pending Internet Fees for Today (Total: PKR $totalAmount):\n$customerListText$moreText"

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            1001,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(shortMessage)
            .setStyle(NotificationCompat.BigTextStyle().bigText(expandedBigText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val UNIQUE_PERIODIC_WORK_NAME = "dolphin_periodic_due_check"
        const val UNIQUE_ONETIME_WORK_NAME = "dolphin_onetime_due_check"
        const val NOTIFICATION_CHANNEL_ID = "dolphin_daily_due_channel"
        const val NOTIFICATION_ID = 1001

        fun schedulePeriodicCheck(context: Context, repeatIntervalHours: Long = 24) {
            try {
                val constraints = Constraints.Builder()
                    .setRequiresBatteryNotLow(false)
                    .build()

                val periodicWorkRequest = PeriodicWorkRequestBuilder<DuePaymentWorker>(
                    repeatIntervalHours, TimeUnit.HOURS,
                    15, TimeUnit.MINUTES
                )
                    .setConstraints(constraints)
                    .addTag("due_payment_check")
                    .build()

                WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    UNIQUE_PERIODIC_WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    periodicWorkRequest
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun triggerImmediateCheck(context: Context) {
            try {
                val oneTimeWorkRequest = OneTimeWorkRequestBuilder<DuePaymentWorker>()
                    .addTag("due_payment_check_immediate")
                    .build()

                WorkManager.getInstance(context).enqueueUniqueWork(
                    UNIQUE_ONETIME_WORK_NAME,
                    ExistingWorkPolicy.REPLACE,
                    oneTimeWorkRequest
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun cancelPeriodicCheck(context: Context) {
            try {
                WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_PERIODIC_WORK_NAME)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
