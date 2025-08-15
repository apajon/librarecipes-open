package com.apajon.librarecipes.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "convives",
    indices = [
        Index(value = ["nom"], unique = true)
    ]
)
data class ConviveEntity(
    @PrimaryKey
    val id: String,
    val nom: String,
    val groupe: String?
)