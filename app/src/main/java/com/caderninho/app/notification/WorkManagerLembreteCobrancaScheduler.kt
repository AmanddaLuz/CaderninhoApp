package com.caderninho.app.notification

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkManagerLembreteCobrancaScheduler @Inject constructor(
    @ApplicationContext context: Context
) : LembreteCobrancaScheduler {

    private val workManager = WorkManager.getInstance(context)

    override fun agendar(clienteId: Long, vencimentoEpochDay: Long) {
        val agora = LocalDateTime.now()
        val execucao = LocalDate.ofEpochDay(vencimentoEpochDay).atTime(HORARIO_LEMBRETE)
        val atrasoMillis = Duration.between(agora, execucao).toMillis().coerceAtLeast(0)
        val dados = Data.Builder()
            .putLong(LembreteCobrancaWorker.CHAVE_CLIENTE_ID, clienteId)
            .putLong(LembreteCobrancaWorker.CHAVE_VENCIMENTO, vencimentoEpochDay)
            .build()
        val requisicao = OneTimeWorkRequestBuilder<LembreteCobrancaWorker>()
            .setInputData(dados)
            .setInitialDelay(atrasoMillis, TimeUnit.MILLISECONDS)
            .addTag(tagCliente(clienteId))
            .build()

        workManager.enqueueUniqueWork(
            nomeTrabalho(clienteId, vencimentoEpochDay),
            ExistingWorkPolicy.REPLACE,
            requisicao
        )
    }

    override fun cancelar(clienteId: Long, vencimentoEpochDay: Long) {
        workManager.cancelUniqueWork(nomeTrabalho(clienteId, vencimentoEpochDay))
    }

    private fun nomeTrabalho(clienteId: Long, vencimentoEpochDay: Long): String =
        "lembrete-cobranca-$clienteId-$vencimentoEpochDay"

    private fun tagCliente(clienteId: Long): String = "lembrete-cobranca-cliente-$clienteId"

    private companion object {
        val HORARIO_LEMBRETE: LocalTime = LocalTime.of(9, 0)
    }
}
