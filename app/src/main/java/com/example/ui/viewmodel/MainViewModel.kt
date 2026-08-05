package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.DolphinApplication
import com.example.data.model.Customer
import com.example.data.model.PaymentRecord
import com.example.util.BackupRestoreHelper
import com.example.util.ReportExporter
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class AppScreen {
    SPLASH,
    DASHBOARD,
    CUSTOMERS,
    REPORTS,
    BACKUP,
    SETTINGS
}

enum class CustomerFilter {
    ALL,
    DUE_TODAY,
    DUE_TOMORROW,
    PAID,
    UNPAID,
    SUSPENDED,
    BY_PACKAGE,
    BY_AREA
}

data class DashboardMetrics(
    val totalCustomers: Int = 0,
    val dueTodayCount: Int = 0,
    val dueTomorrowCount: Int = 0,
    val paidCustomersCount: Int = 0,
    val unpaidCustomersCount: Int = 0,
    val suspendedCustomersCount: Int = 0,
    val totalCollection: Double = 0.0,
    val todayCollectionAmount: Double = 0.0,
    val monthlyCollection: Double = 0.0,
    val pendingAmount: Double = 0.0,
    val todayCollectionList: List<Customer> = emptyList()
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as DolphinApplication
    private val repository = app.repository
    val settingsManager = app.settingsManager

    private val _currentScreen = MutableStateFlow(AppScreen.SPLASH)
    val currentScreen: StateFlow<AppScreen> = _currentScreen

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedFilter = MutableStateFlow(CustomerFilter.ALL)
    val selectedFilter: StateFlow<CustomerFilter> = _selectedFilter

    private val _selectedPackageFilter = MutableStateFlow("")
    val selectedPackageFilter: StateFlow<String> = _selectedPackageFilter

    private val _selectedAreaFilter = MutableStateFlow("")
    val selectedAreaFilter: StateFlow<String> = _selectedAreaFilter

    // Dialog state holders
    private val _customerForDetail = MutableStateFlow<Customer?>(null)
    val customerForDetail: StateFlow<Customer?> = _customerForDetail

    private val _customerForEdit = MutableStateFlow<Customer?>(null)
    val customerForEdit: StateFlow<Customer?> = _customerForEdit
    val isAddCustomerDialogOpen = MutableStateFlow(false)

    private val _customerForPayments = MutableStateFlow<Customer?>(null)
    val customerForPayments: StateFlow<Customer?> = _customerForPayments

    private val _customerToDelete = MutableStateFlow<Customer?>(null)
    val customerToDelete: StateFlow<Customer?> = _customerToDelete

    // App Lock
    val isAppLocked = MutableStateFlow(settingsManager.isAppLockEnabled())

    // All raw data
    val allCustomers: StateFlow<List<Customer>> = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPayments: StateFlow<List<PaymentRecord>> = repository.allPaymentRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered customer list
    val filteredCustomers: StateFlow<List<Customer>> = combine(
        allCustomers,
        searchQuery,
        selectedFilter,
        selectedPackageFilter,
        selectedAreaFilter
    ) { customers, query, filter, pkgFilter, areaFilter ->
        val cal = Calendar.getInstance()
        val todayDay = cal.get(Calendar.DAY_OF_MONTH)
        val tomorrowDay = if (todayDay >= 28) 1 else todayDay + 1

        customers.filter { customer ->
            // Search criteria: Name, Phone, Customer ID/Code, Area
            val matchesQuery = query.isBlank() ||
                    customer.name.contains(query, ignoreCase = true) ||
                    customer.phoneNumber.contains(query, ignoreCase = true) ||
                    customer.customerCode.contains(query, ignoreCase = true) ||
                    customer.area.contains(query, ignoreCase = true) ||
                    customer.id.toString().contains(query)

            val matchesFilter = when (filter) {
                CustomerFilter.ALL -> !customer.isSuspended
                CustomerFilter.DUE_TODAY -> !customer.isSuspended && customer.dueDateDay == todayDay && !customer.isPaidThisMonth
                CustomerFilter.DUE_TOMORROW -> !customer.isSuspended && customer.dueDateDay == tomorrowDay && !customer.isPaidThisMonth
                CustomerFilter.PAID -> !customer.isSuspended && customer.isPaidThisMonth
                CustomerFilter.UNPAID -> !customer.isSuspended && !customer.isPaidThisMonth
                CustomerFilter.SUSPENDED -> customer.isSuspended
                CustomerFilter.BY_PACKAGE -> !customer.isSuspended && (pkgFilter.isEmpty() || customer.internetPackage.equals(pkgFilter, ignoreCase = true))
                CustomerFilter.BY_AREA -> !customer.isSuspended && (areaFilter.isEmpty() || customer.area.equals(areaFilter, ignoreCase = true))
            }

            matchesQuery && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dashboard metrics (Active non-suspended customers)
    val dashboardMetrics: StateFlow<DashboardMetrics> = combine(
        allCustomers,
        allPayments
    ) { customers, payments ->
        val cal = Calendar.getInstance()
        val todayDay = cal.get(Calendar.DAY_OF_MONTH)
        val tomorrowDay = if (todayDay >= 28) 1 else todayDay + 1
        val todayDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val activeCustomers = customers.filter { !it.isSuspended }
        val suspendedCustomers = customers.filter { it.isSuspended }

        val total = activeCustomers.size
        val dueToday = activeCustomers.filter { it.dueDateDay == todayDay && !it.isPaidThisMonth }
        val dueTomorrow = activeCustomers.filter { it.dueDateDay == tomorrowDay && !it.isPaidThisMonth }
        val paidList = activeCustomers.filter { it.isPaidThisMonth }
        val unpaidList = activeCustomers.filter { !it.isPaidThisMonth }

        val monthlyCollection = paidList.sumOf { it.monthlyFee }
        val pendingAmount = unpaidList.sumOf { it.monthlyFee }

        val totalCollection = payments.sumOf { it.amount }
        val todayCollectionAmount = payments.filter { it.paymentDate == todayDateStr }.sumOf { it.amount }

        val todayCollectionList = activeCustomers.filter {
            it.dueDateDay == todayDay || (it.isPaidThisMonth && it.lastPaidDate == todayDateStr)
        }

        DashboardMetrics(
            totalCustomers = total,
            dueTodayCount = dueToday.size,
            dueTomorrowCount = dueTomorrow.size,
            paidCustomersCount = paidList.size,
            unpaidCustomersCount = unpaidList.size,
            suspendedCustomersCount = suspendedCustomers.size,
            totalCollection = totalCollection,
            todayCollectionAmount = todayCollectionAmount,
            monthlyCollection = monthlyCollection,
            pendingAmount = pendingAmount,
            todayCollectionList = todayCollectionList
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardMetrics())

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(filter: CustomerFilter) {
        _selectedFilter.value = filter
    }

    fun setPackageFilter(pkg: String) {
        _selectedPackageFilter.value = pkg
        _selectedFilter.value = CustomerFilter.BY_PACKAGE
    }

    fun setAreaFilter(area: String) {
        _selectedAreaFilter.value = area
        _selectedFilter.value = CustomerFilter.BY_AREA
    }

    fun showCustomerDetail(customer: Customer?) {
        _customerForDetail.value = customer
    }

    fun showEditCustomer(customer: Customer?) {
        _customerForEdit.value = customer
    }

    fun showPaymentsForCustomer(customer: Customer?) {
        _customerForPayments.value = customer
    }

    fun saveCustomer(customer: Customer) {
        viewModelScope.launch {
            if (customer.id == 0L) {
                repository.addCustomer(customer)
            } else {
                repository.updateCustomer(customer)
            }
            _customerForEdit.value = null
            isAddCustomerDialogOpen.value = false
        }
    }

    fun showDeleteConfirmation(customer: Customer?) {
        _customerToDelete.value = customer
    }

    fun confirmDeleteCustomer() {
        val target = _customerToDelete.value ?: return
        viewModelScope.launch {
            repository.deleteCustomer(target)
            if (_customerForDetail.value?.id == target.id) {
                _customerForDetail.value = null
            }
            _customerToDelete.value = null
        }
    }

    fun deleteCustomer(customer: Customer) {
        showDeleteConfirmation(customer)
    }

    fun suspendCustomer(customer: Customer) {
        viewModelScope.launch {
            val updated = customer.copy(isSuspended = true)
            repository.updateCustomer(updated)
            if (_customerForDetail.value?.id == customer.id) {
                _customerForDetail.value = updated
            }
        }
    }

    fun activateCustomer(customer: Customer) {
        viewModelScope.launch {
            val updated = customer.copy(isSuspended = false)
            repository.updateCustomer(updated)
            if (_customerForDetail.value?.id == customer.id) {
                _customerForDetail.value = updated
            }
        }
    }

    fun markCustomerPaid(customer: Customer) {
        viewModelScope.launch {
            repository.markAsPaid(customer)
            if (_customerForDetail.value?.id == customer.id) {
                _customerForDetail.value = customer.copy(isPaidThisMonth = true)
            }
        }
    }

    fun markCustomerUnpaid(customer: Customer) {
        viewModelScope.launch {
            repository.markAsUnpaid(customer)
            if (_customerForDetail.value?.id == customer.id) {
                _customerForDetail.value = customer.copy(isPaidThisMonth = false)
            }
        }
    }

    fun togglePaymentStatus(customer: Customer) {
        if (customer.isPaidThisMonth) {
            markCustomerUnpaid(customer)
        } else {
            markCustomerPaid(customer)
        }
    }

    fun getPaymentsForCustomerFlow(customerId: Long): Flow<List<PaymentRecord>> {
        return repository.getPaymentsForCustomer(customerId)
    }

    fun exportReportPdf(title: String, filteredOnly: Boolean = false): File? {
        val list = if (filteredOnly) filteredCustomers.value else allCustomers.value
        return ReportExporter.generatePdfReport(
            context = getApplication(),
            title = title,
            customers = list,
            payments = allPayments.value,
            currency = settingsManager.getCurrency()
        )
    }

    fun exportReportCsv(title: String, filteredOnly: Boolean = false): File? {
        val list = if (filteredOnly) filteredCustomers.value else allCustomers.value
        return ReportExporter.generateCsvReport(
            context = getApplication(),
            title = title,
            customers = list,
            currency = settingsManager.getCurrency()
        )
    }

    fun shareReportFile(file: File, mimeType: String) {
        ReportExporter.shareFile(getApplication(), file, mimeType)
    }

    fun performBackup(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val file = BackupRestoreHelper.createBackup(getApplication())
            if (file != null) {
                onResult(true, "Backup saved successfully to ${file.name}")
            } else {
                onResult(false, "Failed to create backup")
            }
        }
    }

    fun performRestore(backupFile: File, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val success = BackupRestoreHelper.restoreBackup(getApplication(), backupFile)
            if (success) {
                onResult(true, "Database restored successfully!")
            } else {
                onResult(false, "Failed to restore database from backup file")
            }
        }
    }

    fun validatePin(pin: String): Boolean {
        val storedPin = settingsManager.getAppPin()
        if (storedPin.isEmpty() || pin == storedPin) {
            isAppLocked.value = false
            return true
        }
        return false
    }
}
