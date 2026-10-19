package com.caderninho.app.notification

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@ExperimentalCoroutinesApi
class ChargeRequestBusTest {

    @Test
    fun `request emits the client id to collectors`() = runTest {
        val bus = ChargeRequestBus()

        bus.requests.test {
            bus.request(clientId = 42L)
            assertEquals(42L, awaitItem())
        }
    }

    @Test
    fun `request emits each client id in order`() = runTest {
        val bus = ChargeRequestBus()

        bus.requests.test {
            bus.request(clientId = 1L)
            assertEquals(1L, awaitItem())
            bus.request(clientId = 2L)
            assertEquals(2L, awaitItem())
        }
    }
}
