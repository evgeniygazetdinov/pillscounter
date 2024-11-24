package com.example.pillscounter.data

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PillDao {
    @Query("SELECT * FROM pills ORDER BY name ASC")
    fun getAllPills(): LiveData<List<Pill>>

    @Query("SELECT * FROM pills WHERE id = :pillId")
    fun getPillById(pillId: Long): LiveData<Pill>

    @Query("SELECT * FROM pills WHERE name LIKE :name LIMIT 1")
    suspend fun findPillByName(name: String): Pill?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPill(pill: Pill): Long

    @Update
    suspend fun updatePill(pill: Pill)

    @Delete
    suspend fun deletePill(pill: Pill)

    @Query("SELECT * FROM pill_takings WHERE pillId = :pillId ORDER BY timestamp DESC")
    fun getTakingsForPill(pillId: Long): LiveData<List<PillTaking>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaking(taking: PillTaking)

    @Query("DELETE FROM pill_takings WHERE pillId = :pillId")
    suspend fun deleteTakingsForPill(pillId: Long)

    @Query("DELETE FROM pill_takings WHERE id = :takingId")
    suspend fun deleteTaking(takingId: Long)

    @Update
    suspend fun updateTaking(taking: PillTaking)
}
