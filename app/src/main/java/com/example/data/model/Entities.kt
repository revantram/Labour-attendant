package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Site/Project/Department folder to group labourers.
 */
@Entity(tableName = "folders")
data class Folder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val colorHex: String = "#3B82F6",
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Labourer entity with individual daily rate, overtime rate, and phone.
 */
@Entity(
    tableName = "labourers",
    foreignKeys = [
        ForeignKey(
            entity = Folder::class,
            parentColumns = ["id"],
            childColumns = ["folderId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("folderId"), Index("phone")]
)
data class Labourer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String, // Linked to mobile number for WhatsApp & worker self-view
    val dailyRate: Double, // Individual daily wage rate (e.g. ₹500, ₹650, ₹800)
    val overtimeHourlyRate: Double = 0.0, // Specific hourly OT rate (if 0, defaults to dailyRate / 8)
    val folderId: Long? = null, // Associated site / folder
    val tradeRole: String = "Labour", // Helper, Mason, Carpenter, Electrician, etc.
    val photoUri: String? = null, // URI or avatar resource indicator
    val joinDate: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
) {
    fun effectiveOtRate(): Double {
        return if (overtimeHourlyRate > 0) overtimeHourlyRate else (dailyRate / 8.0)
    }
}

/**
 * Attendance status enum: Full Day, Half Day, Absent, Paid Leave.
 */
enum class AttendanceStatus(val label: String, val weight: Double) {
    PRESENT("Full Day", 1.0),
    HALF_DAY("Half Day", 0.5),
    ABSENT("Absent", 0.0),
    PAID_LEAVE("Paid Leave", 1.0);

    companion object {
        fun fromString(value: String): AttendanceStatus {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: PRESENT
        }
    }
}

/**
 * Daily Attendance record for a specific labourer and date.
 */
@Entity(
    tableName = "attendance_records",
    foreignKeys = [
        ForeignKey(
            entity = Labourer::class,
            parentColumns = ["id"],
            childColumns = ["labourerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["labourerId", "dateString"], unique = true),
        Index("dateString")
    ]
)
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val labourerId: Long,
    val dateString: String, // Format: "YYYY-MM-DD"
    val status: String, // PRESENT, HALF_DAY, ABSENT, PAID_LEAVE
    val overtimeHours: Double = 0.0, // Overtime hours (e.g. 1.0, 2.0, 3.5)
    val notes: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Advance payment or cash deduction given to labourer during the month.
 */
@Entity(
    tableName = "payment_records",
    foreignKeys = [
        ForeignKey(
            entity = Labourer::class,
            parentColumns = ["id"],
            childColumns = ["labourerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("labourerId"), Index("dateString")]
)
data class PaymentRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val labourerId: Long,
    val amount: Double,
    val type: String = "ADVANCE", // ADVANCE, BONUS, ADJUSTMENT
    val dateString: String, // Format: "YYYY-MM-DD"
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Aggregated Monthly Calculation for Salary Slip.
 */
data class MonthlySlip(
    val labourer: Labourer,
    val folderName: String,
    val year: Int,
    val month: Int, // 1-12
    val monthName: String,
    val fullDays: Int,
    val halfDays: Int,
    val absentDays: Int,
    val paidLeaveDays: Int,
    val totalEffectiveDays: Double,
    val totalOtHours: Double,
    val regularPay: Double,
    val overtimePay: Double,
    val grossEarnings: Double,
    val totalAdvanceDeductions: Double,
    val netPayable: Double,
    val dailyAttendanceList: List<AttendanceRecord>,
    val advanceRecords: List<PaymentRecord>
)
