package com.caderninho.app.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.caderninho.app.MainActivity
import com.caderninho.app.R

object NotificacaoCobranca {

    const val CANAL_ID = "lembretes_cobranca"
    const val EXTRA_CLIENTE_ID = "cobranca_cliente_id"

    fun criarCanal(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val canal = NotificationChannel(
            CANAL_ID,
            "Lembretes de cobrança",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Avisos de vendas com pagamento previsto para hoje"
            lockscreenVisibility = android.app.Notification.VISIBILITY_PRIVATE
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
    }

    fun exibir(context: Context, clienteId: Long, clienteNome: String, quantidade: Int, data: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra(EXTRA_CLIENTE_ID, clienteId)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            identificador(clienteId, data),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val texto = if (quantidade == 1) {
            "$clienteNome tem uma venda prevista para cobrança."
        } else {
            "$clienteNome tem $quantidade vendas previstas para cobrança."
        }
        val notificacao = NotificationCompat.Builder(context, CANAL_ID)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle("Cobrança prevista para hoje")
            .setContentText(texto)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .build()
        NotificationManagerCompat.from(context).notify(identificador(clienteId, data), notificacao)
    }

    private fun identificador(clienteId: Long, data: Long): Int =
        "$clienteId-$data".hashCode()
}
