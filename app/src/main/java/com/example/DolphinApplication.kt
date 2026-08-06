package com.example

import android.app.Application
import com.example.data.AppDatabase
import com.example.data.SettingsManager
import com.example.data.repository.CustomerRepository
import com.example.notification.DailyDueReceiver
import com.example.notification.DuePaymentWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DolphinApplication : Application() {
    lateinit var database: AppDatabase
        private set

    lateinit var repository: CustomerRepository
        private set

    lateinit var settingsManager: SettingsManager
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(this)
        repository = CustomerRepository(database.customerDao(), database.paymentRecordDao())
        settingsManager = SettingsManager(this)

        CoroutineScope(Dispatchers.IO).launch {
            repository.prepopulateIfEmpty()
        }

        // Schedule WorkManager periodic task for daily due fee check
        DuePaymentWorker.schedulePeriodicCheck(this)

        // Schedule daily notification at configured time
        val timeStr = settingsManager.getNotificationTime()
        val parts = timeStr.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 9
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        DailyDueReceiver.scheduleDailyNotification(this, hour, minute)
    }
}
