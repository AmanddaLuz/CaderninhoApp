package com.caderninho.app.data.local

import androidx.room.TypeConverter
import com.caderninho.app.domain.model.FormaPagamento
import com.caderninho.app.domain.model.StatusPagamento

/** Conversores para o Room persistir os enums do domínio. */
class Converters {
    @TypeConverter
    fun fromFormaPagamento(value: FormaPagamento): String = value.name

    @TypeConverter
    fun toFormaPagamento(value: String): FormaPagamento = FormaPagamento.valueOf(value)

    @TypeConverter
    fun fromStatusPagamento(value: StatusPagamento): String = value.name

    @TypeConverter
    fun toStatusPagamento(value: String): StatusPagamento = StatusPagamento.valueOf(value)
}
