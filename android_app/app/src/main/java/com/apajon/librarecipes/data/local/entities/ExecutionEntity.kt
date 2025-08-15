package com.apajon.librarecipes.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "executions",
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recetteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["recetteId", "dateExecution"], unique = true)
    ]
)
data class ExecutionEntity(
    @PrimaryKey
    val id: String,
    val recetteId: String,
    val dateExecution: String,
    val nombreConvives: Int?
)