package com.example

import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.Labourer
import com.example.data.model.MonthlySlip
import com.example.util.DateUtils
import com.example.util.WhatsAppHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testLabourerEffectiveOtRate() {
        val labourerAuto = Labourer(
            id = 1,
            name = "Ramesh",
            phone = "9876543210",
            dailyRate = 800.0,
            overtimeHourlyRate = 0.0 // auto (800 / 8 = 100)
        )
        assertEquals(100.0, labourerAuto.effectiveOtRate(), 0.001)

        val labourerCustom = Labourer(
            id = 2,
            name = "Dinesh",
            phone = "9876543211",
            dailyRate = 600.0,
            overtimeHourlyRate = 90.0
        )
        assertEquals(90.0, labourerCustom.effectiveOtRate(), 0.001)
    }

    @Test
    fun testPhoneSanitizationForWhatsApp() {
        assertEquals("919876543210", WhatsAppHelper.cleanPhoneNumber("9876543210"))
        assertEquals("919876543210", WhatsAppHelper.cleanPhoneNumber("+91 98765-43210"))
        assertEquals("919876543210", WhatsAppHelper.cleanPhoneNumber("09876543210"))
    }

    @Test
    fun testAttendanceStatusWeights() {
        assertEquals(1.0, AttendanceStatus.PRESENT.weight, 0.001)
        assertEquals(0.5, AttendanceStatus.HALF_DAY.weight, 0.001)
        assertEquals(0.0, AttendanceStatus.ABSENT.weight, 0.001)
    }

    @Test
    fun testDateUtilsShift() {
        val today = "2026-10-29"
        val tomorrow = DateUtils.shiftDate(today, 1)
        val yesterday = DateUtils.shiftDate(today, -1)
        assertEquals("2026-10-30", tomorrow)
        assertEquals("2026-10-28", yesterday)
    }

    @Test
    fun testMonthlySlipFormatting() {
        val labourer = Labourer(
            id = 1,
            name = "Mukesh Kumar",
            phone = "9876543210",
            dailyRate = 600.0,
            overtimeHourlyRate = 75.0,
            tradeRole = "Mason"
        )

        val slip = MonthlySlip(
            labourer = labourer,
            folderName = "Site A - Central Mall",
            year = 2026,
            month = 10,
            monthName = "October",
            fullDays = 20,
            halfDays = 4,
            absentDays = 6,
            paidLeaveDays = 0,
            totalEffectiveDays = 22.0,
            totalOtHours = 10.0,
            regularPay = (20 * 600.0) + (4 * 300.0), // 12000 + 1200 = 13200
            overtimePay = 10.0 * 75.0, // 750
            grossEarnings = 13950.0,
            totalAdvanceDeductions = 2000.0,
            netPayable = 11950.0,
            dailyAttendanceList = emptyList(),
            advanceRecords = emptyList()
        )

        val message = WhatsAppHelper.generateSlipMessage(slip, "XYZ Builders", "9988776655")
        assertTrue(message.contains("MONTHLY ATTENDANCE & WAGE SLIP"))
        assertTrue(message.contains("Mukesh Kumar"))
        assertTrue(message.contains("Full Days (P)"))
        assertTrue(message.contains("Half Days (H)"))
        assertTrue(message.contains("NET PAYABLE AMOUNT"))
    }
}
