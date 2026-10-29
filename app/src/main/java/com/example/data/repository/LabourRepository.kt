package com.example.data.repository

import com.example.data.local.LabourDao
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.Folder
import com.example.data.model.Labourer
import com.example.data.model.MonthlySlip
import com.example.data.model.PaymentRecord
import com.example.util.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class LabourRepository(private val dao: LabourDao) {

    val allFolders: Flow<List<Folder>> = dao.getAllFolders()
    val allLabourers: Flow<List<Labourer>> = dao.getAllLabourers()

    fun getAttendanceForDate(dateString: String): Flow<List<AttendanceRecord>> {
        return dao.getAttendanceForDate(dateString)
    }

    fun getAttendanceForLabourerMonth(labourerId: Long, monthKey: String): Flow<List<AttendanceRecord>> {
        return dao.getAttendanceForLabourerMonth(labourerId, monthKey)
    }

    fun getPaymentsForLabourerMonth(labourerId: Long, monthKey: String): Flow<List<PaymentRecord>> {
        return dao.getPaymentsForLabourerMonth(labourerId, monthKey)
    }

    suspend fun findLabourerByPhone(phoneQuery: String): Labourer? = withContext(Dispatchers.IO) {
        val clean = phoneQuery.replace(Regex("[^0-9]"), "")
        if (clean.isBlank()) return@withContext null
        // Try exact match or suffix match (e.g. last 10 digits)
        val exact = dao.getLabourerByPhone(clean)
        if (exact != null) return@withContext exact
        val shortPhone = if (clean.length > 10) clean.takeLast(10) else clean
        dao.findLabourerByPhoneQuery(shortPhone)
    }

    suspend fun getLabourerById(id: Long): Labourer? = withContext(Dispatchers.IO) {
        dao.getLabourerById(id)
    }

    suspend fun saveFolder(folder: Folder): Long = withContext(Dispatchers.IO) {
        dao.insertFolder(folder)
    }

    suspend fun deleteFolder(folder: Folder) = withContext(Dispatchers.IO) {
        dao.deleteFolder(folder)
    }

    suspend fun saveLabourer(labourer: Labourer): Long = withContext(Dispatchers.IO) {
        dao.insertLabourer(labourer)
    }

    suspend fun deleteLabourer(labourer: Labourer) = withContext(Dispatchers.IO) {
        dao.deleteLabourer(labourer)
    }

    suspend fun setAttendance(
        labourerId: Long,
        dateString: String,
        status: AttendanceStatus,
        overtimeHours: Double = 0.0,
        notes: String = ""
    ) = withContext(Dispatchers.IO) {
        val record = AttendanceRecord(
            labourerId = labourerId,
            dateString = dateString,
            status = status.name,
            overtimeHours = overtimeHours,
            notes = notes,
            updatedAt = System.currentTimeMillis()
        )
        dao.insertOrUpdateAttendance(record)
    }

    suspend fun setOvertime(
        labourerId: Long,
        dateString: String,
        currentStatus: String,
        overtimeHours: Double
    ) = withContext(Dispatchers.IO) {
        val record = AttendanceRecord(
            labourerId = labourerId,
            dateString = dateString,
            status = currentStatus.ifBlank { AttendanceStatus.PRESENT.name },
            overtimeHours = overtimeHours,
            updatedAt = System.currentTimeMillis()
        )
        dao.insertOrUpdateAttendance(record)
    }

    suspend fun bulkMarkAttendance(
        labourers: List<Labourer>,
        dateString: String,
        status: AttendanceStatus
    ) = withContext(Dispatchers.IO) {
        val existing = dao.getAttendanceForDateSync(dateString).associateBy { it.labourerId }
        val toInsert = labourers.map { labourer ->
            val prev = existing[labourer.id]
            AttendanceRecord(
                id = prev?.id ?: 0,
                labourerId = labourer.id,
                dateString = dateString,
                status = status.name,
                overtimeHours = prev?.overtimeHours ?: 0.0,
                notes = prev?.notes ?: "",
                updatedAt = System.currentTimeMillis()
            )
        }
        dao.insertOrUpdateAttendanceBatch(toInsert)
    }

    suspend fun addPayment(payment: PaymentRecord): Long = withContext(Dispatchers.IO) {
        dao.insertPayment(payment)
    }

    suspend fun deletePayment(paymentId: Long) = withContext(Dispatchers.IO) {
        dao.deletePaymentById(paymentId)
    }

    suspend fun computeMonthlySlip(
        labourer: Labourer,
        folderName: String,
        monthKey: String
    ): MonthlySlip = withContext(Dispatchers.IO) {
        val (year, month) = DateUtils.parseYearMonth(monthKey)
        val monthName = DateUtils.formatMonthDisplay(monthKey)

        val attendanceList = dao.getAttendanceForLabourerMonthSync(labourer.id, monthKey)
        val paymentList = dao.getPaymentsForLabourerMonthSync(labourer.id, monthKey)

        var fullDays = 0
        var halfDays = 0
        var absentDays = 0
        var paidLeaveDays = 0
        var totalOtHours = 0.0

        for (rec in attendanceList) {
            when (AttendanceStatus.fromString(rec.status)) {
                AttendanceStatus.PRESENT -> fullDays++
                AttendanceStatus.HALF_DAY -> halfDays++
                AttendanceStatus.ABSENT -> absentDays++
                AttendanceStatus.PAID_LEAVE -> paidLeaveDays++
            }
            if (rec.overtimeHours > 0) {
                totalOtHours += rec.overtimeHours
            }
        }

        val totalEffectiveDays = fullDays + (halfDays * 0.5) + paidLeaveDays
        val regularPay = (fullDays * labourer.dailyRate) +
                (halfDays * (labourer.dailyRate / 2.0)) +
                (paidLeaveDays * labourer.dailyRate)
        val overtimePay = totalOtHours * labourer.effectiveOtRate()
        val grossEarnings = regularPay + overtimePay

        val totalAdvanceDeductions = paymentList
            .filter { it.type == "ADVANCE" || it.type == "DEDUCTION" }
            .sumOf { it.amount }

        val netPayable = (grossEarnings - totalAdvanceDeductions).coerceAtLeast(0.0)

        MonthlySlip(
            labourer = labourer,
            folderName = folderName,
            year = year,
            month = month,
            monthName = monthName,
            fullDays = fullDays,
            halfDays = halfDays,
            absentDays = absentDays,
            paidLeaveDays = paidLeaveDays,
            totalEffectiveDays = totalEffectiveDays,
            totalOtHours = totalOtHours,
            regularPay = regularPay,
            overtimePay = overtimePay,
            grossEarnings = grossEarnings,
            totalAdvanceDeductions = totalAdvanceDeductions,
            netPayable = netPayable,
            dailyAttendanceList = attendanceList,
            advanceRecords = paymentList
        )
    }

    suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        val existingLabourers = dao.getAllLabourers()
        // Check if there are labourers by getting sync or inserting sample
        // We will insert starter folders and labourers if table is empty
        val sampleFolder1 = dao.insertFolder(
            Folder(name = "Site A - Central Mall", description = "Main commercial construction site", colorHex = "#2563EB")
        )
        val sampleFolder2 = dao.insertFolder(
            Folder(name = "Site B - Green Villa", description = "Residential villa project", colorHex = "#059669")
        )

        val l1 = dao.insertLabourer(
            Labourer(
                name = "Ramesh Kumar",
                phone = "9876543210",
                dailyRate = 600.0,
                overtimeHourlyRate = 75.0,
                folderId = sampleFolder1,
                tradeRole = "Head Mason"
            )
        )

        val l2 = dao.insertLabourer(
            Labourer(
                name = "Suresh Verma",
                phone = "9812345678",
                dailyRate = 500.0,
                overtimeHourlyRate = 65.0,
                folderId = sampleFolder1,
                tradeRole = "Helper"
            )
        )

        val l3 = dao.insertLabourer(
            Labourer(
                name = "Dinesh Prajapati",
                phone = "9765432109",
                dailyRate = 750.0,
                overtimeHourlyRate = 95.0,
                folderId = sampleFolder2,
                tradeRole = "Carpenter"
            )
        )

        val l4 = dao.insertLabourer(
            Labourer(
                name = "Mukesh Yadav",
                phone = "9988776655",
                dailyRate = 550.0,
                overtimeHourlyRate = 70.0,
                folderId = sampleFolder2,
                tradeRole = "Electrician Helper"
            )
        )

        // Seed some attendance records for current month so slips and stats look great immediately
        val today = DateUtils.todayDateString()
        val currentMonth = DateUtils.currentMonthKey()
        val daysInMonth = DateUtils.getDaysInMonth(currentMonth)

        // Add attendance for up to today
        val sampleDays = daysInMonth.filter { it <= today }.takeLast(10)
        sampleDays.forEachIndexed { index, dateStr ->
            // Ramesh
            val statusL1 = when {
                index % 6 == 0 -> AttendanceStatus.HALF_DAY
                index % 7 == 3 -> AttendanceStatus.ABSENT
                else -> AttendanceStatus.PRESENT
            }
            val otL1 = if (index % 3 == 0) 2.0 else 0.0
            dao.insertOrUpdateAttendance(
                AttendanceRecord(
                    labourerId = l1,
                    dateString = dateStr,
                    status = statusL1.name,
                    overtimeHours = otL1
                )
            )

            // Suresh
            val statusL2 = if (index % 5 == 0) AttendanceStatus.HALF_DAY else AttendanceStatus.PRESENT
            dao.insertOrUpdateAttendance(
                AttendanceRecord(
                    labourerId = l2,
                    dateString = dateStr,
                    status = statusL2.name,
                    overtimeHours = if (index % 4 == 0) 1.5 else 0.0
                )
            )

            // Dinesh
            dao.insertOrUpdateAttendance(
                AttendanceRecord(
                    labourerId = l3,
                    dateString = dateStr,
                    status = AttendanceStatus.PRESENT.name,
                    overtimeHours = if (index % 2 == 0) 2.0 else 0.0
                )
            )

            // Mukesh
            val statusL4 = if (index == 2) AttendanceStatus.ABSENT else AttendanceStatus.PRESENT
            dao.insertOrUpdateAttendance(
                AttendanceRecord(
                    labourerId = l4,
                    dateString = dateStr,
                    status = statusL4.name,
                    overtimeHours = 0.0
                )
            )
        }

        // Add a sample advance payment for Ramesh
        if (sampleDays.isNotEmpty()) {
            dao.insertPayment(
                PaymentRecord(
                    labourerId = l1,
                    amount = 1500.0,
                    type = "ADVANCE",
                    dateString = sampleDays.first(),
                    notes = "Emergency medicine advance"
                )
            )
        }
    }
}
