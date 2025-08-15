package com.apajon.librarecipes.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "feedback_execution",
    foreignKeys = [
        ForeignKey(
            entity = ExecutionEntity::class,
            parentColumns = ["id"],
            childColumns = ["executionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ConviveEntity::class,
            parentColumns = ["id"],
            childColumns = ["conviveId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["executionId"]),
        Index(value = ["conviveId"])
    ]
)
data class FeedbackExecutionEntity(
    @PrimaryKey
    val id: String,
    val executionId: String,
    val conviveId: String,
    val statut: String
)