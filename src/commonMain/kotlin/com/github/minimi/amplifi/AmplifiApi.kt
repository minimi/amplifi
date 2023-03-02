package com.github.minimi.amplifi

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

interface AmplifiApi {
    suspend fun getLoginToken(): String
    suspend fun login()
    suspend fun getInfoToken(): String
    suspend fun getDevices(): List<Map<String, JsonObject>>
    suspend fun getInfo(): List<Map<String, JsonObject>>
    suspend fun initClient(force: Boolean = false)
    suspend fun getRouterMacAddress(): String
    suspend fun getWanPortInfo(): String
    suspend fun testConnection(): Boolean

    companion object {
        val client = HttpClient {
            install(Logging) {
                level = LogLevel.ALL
            }
            install(HttpCookies)
            install(ContentNegotiation) {
                json(Json {
                    isLenient = true
                    prettyPrint = true
                })
            }
        }
    }
}