package com.caderninho.app.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import java.net.URLEncoder

/**
 * Abre o WhatsApp com uma mensagem de cobrança pré-preenchida, usando o
 * link universal `wa.me` (Intent simples, sem necessidade de API paga).
 */
object WhatsAppUtil {

    private const val TAG = "WhatsAppUtil"

    fun enviarCobranca(context: Context, telefone: String, mensagem: String) {
        val numero = normalizarTelefone(telefone)
        val texto = URLEncoder.encode(mensagem, "UTF-8")
        val uri = Uri.parse("https://wa.me/$numero?text=$texto")
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
    internal fun normalizarTelefone(telefone: String): String {
        val digitos = telefone.filter { it.isDigit() }
        return if (digitos.startsWith("55")) digitos else "55$digitos"
    }
}
