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
import com.caderninho.app.notification.ChargeNotification
import com.caderninho.app.ui.components.NotebookPage
import com.caderninho.app.ui.navigation.LedgerNavHost
import com.caderninho.app.ui.theme.LedgerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val clientToCharge = MutableStateFlow<Long?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        processCharge(intent)
        enableEdgeToEdge()
        setContent {
            val clientId by clientToCharge.collectAsState()
            LedgerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NotebookPage {
                        LedgerNavHost(
                            clientToCharge = clientId,
                            onChargeConsumed = {
                                clientToCharge.value = null
                                intent.removeExtra(ChargeNotification.CLIENT_ID_EXTRA)
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        processCharge(intent)
    }

    private fun processCharge(intent: Intent?) {
        val clientId = intent?.getLongExtra(ChargeNotification.CLIENT_ID_EXTRA, INVALID_ID)
        if (clientId != null && clientId != INVALID_ID) {
            clientToCharge.value = clientId
        }
    }

    private companion object {
        const val INVALID_ID = -1L
    }
}
