package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AttendanceRecord
import com.example.data.model.Folder
import com.example.data.model.Labourer
import com.example.data.model.PaymentRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface LabourDao {

    // Folders
    @Query("SELECT * FROM folders ORDER BY name ASC")
    fun getAllFolders(): Flow<List<Folder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: Folder): Long

    @Update
    suspend fun updateFolder(folder: Folder)

    @Delete
    suspend fun deleteFolder(folder: Folder)

    // Labourers
    @Query("SELECT * FROM labourers WHERE isActive = 1 ORDER BY name ASC")
    fun getAllLabourers(): Flow<List<Labourer>>

    @Query("SELECT * FROM labourers WHERE folderId = :folderId AND isActive = 1 ORDER BY name ASC")
    fun getLabourersByFolder(folderId: Long): Flow<List<Labourer>>

    @Query("SELECT * FROM labourers WHERE id = :id LIMIT 1")
    suspend fun getLabourerById(id: Long): Labourer?

    @Query("SELECT * FROM labourers WHERE phone = :phone LIMIT 1")
    suspend fun getLabourerByPhone(phone: String): Labourer?

    @Query("SELECT * FROM labourers WHERE phone LIKE '%' || :phoneQuery || '%' LIMIT 1")
    suspend fun findLabourerByPhoneQuery(phoneQuery: String): Labourer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLabourer(labourer: Labourer): Long

    @Update
    suspend fun updateLabourer(labourer: Labourer)

    @Delete
    suspend fun deleteLabourer(labourer: Labourer)

    // Attendance
    @Query("SELECT * FROM attendance_records WHERE dateString = :dateString")
    fun getAttendanceForDate(dateString: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE dateString = :dateString")
    suspend fun getAttendanceForDateSync(dateString: String): List<AttendanceRecord>

    @Query("SELECT * FROM attendance_records WHERE labourerId = :labourerId AND dateString LIKE :monthPrefix || '%' ORDER BY dateString ASC")
    fun getAttendanceForLabourerMonth(labourerId: Long, monthPrefix: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE labourerId = :labourerId AND dateString LIKE :monthPrefix || '%' ORDER BY dateString ASC")
    suspend fun getAttendanceForLabourerMonthSync(labourerId: Long, monthPrefix: String): List<AttendanceRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAttendance(record: AttendanceRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAttendanceBatch(records: List<AttendanceRecord>)

    @Query("DELETE FROM attendance_records WHERE labourerId = :labourerId AND dateString = :dateString")
    suspend fun deleteAttendanceByDate(labourerId: Long, dateString: String)

    // Payments / Advances
    @Query("SELECT * FROM payment_records WHERE labourerId = :labourerId AND dateString LIKE :monthPrefix || '%' ORDER BY dateString ASC")
    fun getPaymentsForLabourerMonth(labourerId: Long, monthPrefix: String): Flow<List<PaymentRecord>>

    @Query("SELECT * FROM payment_records WHERE labourerId = :labourerId AND dateString LIKE :monthPrefix || '%' ORDER BY dateString ASC")
    suspend fun getPaymentsForLabourerMonthSync(labourerId: Long, monthPrefix: String): List<PaymentRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentRecord): Long

    @Query("DELETE FROM payment_records WHERE id = :id")
    suspend fun deletePaymentById(id: Long)
}
