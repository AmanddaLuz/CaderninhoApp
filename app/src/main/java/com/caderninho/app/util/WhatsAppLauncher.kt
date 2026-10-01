package com.caderninho.app.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.net.toUri
import java.net.URLEncoder

/**
 * Abre o WhatsApp com uma mensagem de cobrança pré-preenchida, usando o
 * link universal `wa.me` (Intent simples, sem necessidade de API paga).
 */
object WhatsAppLauncher {

    private const val TAG = "WhatsAppLauncher"

    fun sendCharge(context: Context, phone: String, message: String) {
        val number = normalizePhone(phone)
        val text = URLEncoder.encode(message, "UTF-8")
        val uri = "https://wa.me/$number?text=$text".toUri()
        val intent = Intent(Intent.ACTION_VIEW, uri)
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // Message content is never logged, only the fact that the launch failed.
            Log.w(TAG, "WhatsApp não está instalado neste aparelho", e)
            Toast.makeText(context, "WhatsApp não encontrado neste aparelho", Toast.LENGTH_SHORT).show()
        }
    }

    /** Normaliza um telefone digitado livremente para o formato `55<ddd><numero>` exigido pelo wa.me. */
    internal fun normalizePhone(phone: String): String {
        val digits = phone.filter { it.isDigit() }
        return if (digits.startsWith("55")) digits else "55$digits"
    }
}
