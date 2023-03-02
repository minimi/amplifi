package com.github.minimi.amplifi

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.fail

@OptIn(ExperimentalCoroutinesApi::class)
internal class AmplifiClientTest {

    private val client = HttpClient(CIO) {
        install(Logging) {
            level = LogLevel.ALL
            logger = object : Logger {
                override fun log(message: String) {
                    println(message)
                }
            }
        }
        install(HttpCookies)
        install(ContentNegotiation) {
            json(Json {
                isLenient = true
                prettyPrint = true
            })
        }
    }

    @Test
    fun check() = runTest {

        val apiClient = AmplifiClient(client, "192.168.119.1", "somePassword")

        val token = apiClient.getLoginToken()
        assertTrue("Login token must be present") { token.isNotBlank() }
        println("Login Token: $token")

        try {
            apiClient.login()
            print("Logged successfully")
        } catch (ex: Throwable) {
            fail(ex.message)
        }

        val infoToken = apiClient.getInfoToken()
        assertTrue("InfoToken must be present") { infoToken.isNotBlank() }
        println(infoToken)

        val devices = apiClient.getDevices()
        assertTrue("devices is not present") { devices.isNotEmpty() }
        println(devices)
    }
}
