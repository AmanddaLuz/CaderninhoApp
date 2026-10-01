package com.caderninho.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.caderninho.app.notification.NotificacaoCobranca
import com.caderninho.app.ui.navigation.CaderninhoNavHost
import com.caderninho.app.ui.theme.CaderninhoTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val clienteParaCobranca = MutableStateFlow<Long?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        processarCobranca(intent)
        enableEdgeToEdge()
        setContent {
            val clienteId by clienteParaCobranca.collectAsState()
            CaderninhoTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CaderninhoNavHost(
                        clienteParaCobranca = clienteId,
                        onCobrancaConsumida = {
                            clienteParaCobranca.value = null
                            intent.removeExtra(NotificacaoCobranca.EXTRA_CLIENTE_ID)
                        }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        processarCobranca(intent)
    }

    private fun processarCobranca(intent: Intent?) {
        val clienteId = intent?.getLongExtra(NotificacaoCobranca.EXTRA_CLIENTE_ID, ID_INVALIDO)
        if (clienteId != null && clienteId != ID_INVALIDO) {
            clienteParaCobranca.value = clienteId
        }
    }

    private companion object {
        const val ID_INVALIDO = -1L
    }
}
