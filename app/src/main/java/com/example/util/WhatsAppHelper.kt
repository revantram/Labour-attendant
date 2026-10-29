package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.data.model.MonthlySlip
import java.net.URLEncoder
import java.text.NumberFormat
import java.util.Locale

object WhatsAppHelper {

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    fun formatCurrency(amount: Double): String {
        return try {
            currencyFormat.format(amount)
        } catch (_: Exception) {
            "₹" + String.format(Locale.getDefault(), "%.2f", amount)
        }
    }

    fun generateSlipMessage(
        slip: MonthlySlip,
        businessName: String = "Labour Management",
        adminPhone: String = ""
    ): String {
        val labourer = slip.labourer
        val sb = StringBuilder()

        sb.append("📋 *MONTHLY ATTENDANCE & WAGE SLIP*\n")
        sb.append("🏢 *${businessName.ifBlank { "Labour In-charge" }}*\n")
        sb.append("📅 *Month:* ${slip.monthName} ${slip.year}\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("👤 *Labourer:* ${labourer.name}\n")
        sb.append("📱 *Mobile:* ${labourer.phone}\n")
        if (labourer.tradeRole.isNotBlank()) {
            sb.append("🛠️ *Trade/Role:* ${labourer.tradeRole}\n")
        }
        if (slip.folderName.isNotBlank()) {
            sb.append("📁 *Site/Folder:* ${slip.folderName}\n")
        }
        sb.append("💵 *Daily Wage Rate:* ${formatCurrency(labourer.dailyRate)} / day\n")
        sb.append("⏱️ *Overtime Rate:* ${formatCurrency(labourer.effectiveOtRate())} / hr\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("📊 *ATTENDANCE BREAKDOWN:*\n")
        sb.append("✅ *Full Days (P):* ${slip.fullDays} days (${formatCurrency(slip.fullDays * labourer.dailyRate)})\n")
        sb.append("🌗 *Half Days (H):* ${slip.halfDays} days (${formatCurrency(slip.halfDays * (labourer.dailyRate / 2.0))})\n")
        sb.append("❌ *Absent (A):* ${slip.absentDays} days\n")
        if (slip.paidLeaveDays > 0) {
            sb.append("🏖️ *Paid Leave:* ${slip.paidLeaveDays} days (${formatCurrency(slip.paidLeaveDays * labourer.dailyRate)})\n")
        }
        sb.append("⏰ *Overtime (OT):* ${String.format(Locale.getDefault(), "%.1f", slip.totalOtHours)} hrs (${formatCurrency(slip.overtimePay)})\n")
        sb.append("────────────────────\n")
        sb.append("💼 *Total Effective Days:* ${String.format(Locale.getDefault(), "%.1f", slip.totalEffectiveDays)} days\n")
        sb.append("💰 *Gross Earnings:* ${formatCurrency(slip.grossEarnings)}\n")

        if (slip.totalAdvanceDeductions > 0) {
            sb.append("💸 *Advance Deductions:* -${formatCurrency(slip.totalAdvanceDeductions)}\n")
            if (slip.advanceRecords.isNotEmpty()) {
                val details = slip.advanceRecords.joinToString(", ") {
                    "${it.dateString}: ${formatCurrency(it.amount)}"
                }
                sb.append("   _(Advances: $details)_\n")
            }
        }

        sb.append("━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("💵 *NET PAYABLE AMOUNT: ${formatCurrency(slip.netPayable)}*\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("📅 Generated on ${DateUtils.formatDisplayDate(DateUtils.todayDateString())}\n")
        if (adminPhone.isNotBlank()) {
            sb.append("📞 For any queries, contact Admin: $adminPhone\n")
        }
        sb.append("📱 _Sent via Labour Attendance App_")

        return sb.toString()
    }

    fun cleanPhoneNumber(rawPhone: String): String {
        val digitsOnly = rawPhone.replace(Regex("[^0-9]"), "")
        return when {
            digitsOnly.length == 10 -> "91$digitsOnly" // Default India country code if 10 digits
            digitsOnly.startsWith("0") && digitsOnly.length == 11 -> "91" + digitsOnly.substring(1)
            else -> digitsOnly
        }
    }

    fun sendSlipOnWhatsApp(
        context: Context,
        phoneNumber: String,
        messageText: String
    ) {
        val cleanPhone = cleanPhoneNumber(phoneNumber)
        try {
            val encodedMessage = URLEncoder.encode(messageText, "UTF-8")
            val whatsappUri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=$encodedMessage")
            val intent = Intent(Intent.ACTION_VIEW, whatsappUri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            // Fallback to generic chooser
            try {
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, messageText)
                    type = "text/plain"
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                val chooser = Intent.createChooser(sendIntent, "Send Wage Slip via:")
                chooser.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(chooser)
            } catch (ex: Exception) {
                Toast.makeText(context, "Could not open messaging app: ${ex.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
