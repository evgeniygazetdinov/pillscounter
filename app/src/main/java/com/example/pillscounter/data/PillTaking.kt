package com.example.pillscounter.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "pill_takings",
    foreignKeys = [
        ForeignKey(
            entity = Pill::class,
            parentColumns = ["id"],
            childColumns = ["pillId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("pillId")]
)
data class PillTaking(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val pillId: Long,
    val timestamp: Date,
    val count: Int
)
