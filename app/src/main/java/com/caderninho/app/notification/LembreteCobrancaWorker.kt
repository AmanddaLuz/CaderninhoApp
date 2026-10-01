package com.caderninho.app.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.caderninho.app.data.repository.CaderninhoRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class LembreteCobrancaWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParameters: WorkerParameters,
    private val repository: CaderninhoRepository
) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result {
        val clienteId = inputData.getLong(CHAVE_CLIENTE_ID, ID_INVALIDO)
        val vencimento = inputData.getLong(CHAVE_VENCIMENTO, DATA_INVALIDA)
        return when {
            clienteId == ID_INVALIDO || vencimento == DATA_INVALIDA -> Result.failure()
            else -> processarLembrete(clienteId, vencimento)
        }
    }

    private suspend fun processarLembrete(clienteId: Long, vencimento: Long): Result {
        val cliente = repository.obterCliente(clienteId)
        val pendencias = repository.obterPendentesNaData(clienteId, vencimento)
        if (cliente != null && pendencias.isNotEmpty()) {
            NotificacaoCobranca.exibir(
                context = applicationContext,
                clienteId = clienteId,
                clienteNome = cliente.nome,
                quantidade = pendencias.size,
                data = vencimento
            )
        }
        return Result.success()
    }

    companion object {
        const val CHAVE_CLIENTE_ID = "cliente_id"
        const val CHAVE_VENCIMENTO = "vencimento_epoch_day"
        private const val ID_INVALIDO = -1L
        private const val DATA_INVALIDA = Long.MIN_VALUE
    }
}
