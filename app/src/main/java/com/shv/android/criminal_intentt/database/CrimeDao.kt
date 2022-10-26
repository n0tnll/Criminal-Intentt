package com.shv.android.criminal_intentt.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.shv.android.criminal_intentt.Crime
import java.util.UUID

@Dao
interface CrimeDao {
    @Query("SELECT * FROM crime")
    fun getCrimes(): LiveData<List<Crime>>

    @Query("SELECT * FROM crime WHERE id=(:id)")
    fun getCrime(id: UUID): LiveData<Crime?>

    @Update
    fun updateCrime(crime: Crime)

    @Insert
    fun addCrime(crime: Crime)

    @Query("DELETE FROM crime")
    fun clearAllCrimes()
}