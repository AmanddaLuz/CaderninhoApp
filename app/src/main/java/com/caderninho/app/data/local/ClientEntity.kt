package com.caderninho.app.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Representa um cliente cadastrado pelo lojista. */
@Entity(
    tableName = "clientes",
    indices = [Index(value = ["cpf"], unique = true)]
)
data class ClientEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "nome")
    val name: String,
    @ColumnInfo(name = "telefone")
    val phone: String,
    val cpf: String? = null,
    @ColumnInfo(name = "observacao")
    val notes: String = "",
    @ColumnInfo(name = "criadoEm")
    val createdAt: Long = System.currentTimeMillis()
)
