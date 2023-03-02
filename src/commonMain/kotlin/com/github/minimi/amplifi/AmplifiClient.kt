package com.github.minimi.amplifi

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.JsonObject

@Suppress("unused")
class AmplifiClient(
    private val client: HttpClient,
    host: String,
    private val password: String,
) : AmplifiApi {

    private val baseUrl = "http://$host"
    private var loginToken: String? = null
    private var infoToken: String? = null
    private var webuiSession: Cookie? = null

    override suspend fun getDevices(): List<Map<String, JsonObject>> {
        return getInfo()
    }

    override suspend fun getLoginToken(): String {
        val resp = client.get("$baseUrl/login.php")

        if (resp.status != HttpStatusCode.OK) {
            throw AmplifiClientException("Expected a response code of 200.")
        }

        val loginPageContent = resp.body<String>()

        val re = "value=\'([A-Za-z0-9]{16})\'".toRegex()

        val tokenSearchResult = re.find(loginPageContent) ?: throw AmplifiClientException("Login token was not found.")

        val token = tokenSearchResult.groups[1]?.value ?: throw AmplifiClientException("Login token was not found.")

        loginToken = token

        return token
    }

    override suspend fun login() {

        val formData = Parameters.build {
            append("token", loginToken ?: throw AmplifiClientException("No token"))
            append("password", password)
        }

        val resp = client.submitForm(
            url = "$baseUrl/login.php",
            formParameters = formData
        )

        if (resp.status != HttpStatusCode.OK)
            throw AmplifiClientException("Expected a response code of 200.")

        val webuiSession = resp
            .setCookie()
            .find { cookie -> cookie.name == "webui-session" }
            ?: throw AmplifiClientException("Authentication failure.")

        this.webuiSession = webuiSession
    }

    override suspend fun getInfoToken(): String {
        if (webuiSession == null) throw AmplifiClientException("Not logged in")

        val resp = client.get("$baseUrl/info.php") {
            cookie(
                name = webuiSession!!.name,
                value = webuiSession!!.value
            )
        }

        if (resp.status != HttpStatusCode.OK)
            throw AmplifiClientException("Expected a response code of 200.")

        val infoPageContent = resp.bodyAsText()

        val re = "token=\'([A-Za-z0-9]{16})\'".toRegex()

        val tokenSearchResult = re.find(infoPageContent) ?: throw AmplifiClientException("Info token was not found.")

        val token = tokenSearchResult.groups[1]?.value ?: throw AmplifiClientException("Info token was not found.")

        this.infoToken = token

        return token
    }

    override suspend fun getInfo(): List<Map<String, JsonObject>> {
        val infoAsyncUrl = "$baseUrl/info-async.php"

        if (webuiSession == null) throw AmplifiClientException("Session not initialized")
        if (infoToken == null) throw AmplifiClientException("No infoToken. Not logged in?")

        val resp = client.post(infoAsyncUrl) {
            setBody("do=full&token=$infoToken")
            cookie(
                name = webuiSession!!.name,
                value = webuiSession!!.value
            )
        }

        try {
            return resp.body()
        } catch (ex: Throwable) {
            handleClientFailure()
            throw AmplifiClientException("Failed to get devices from router.", ex)
        }
    }

    fun handleClientFailure() {
        loginToken = null
        infoToken = null
    }

    //
    override suspend fun initClient(force: Boolean) {
        if (force || loginToken.isNullOrBlank() || infoToken.isNullOrBlank()) {
            try {
                if (force) webuiSession = null
                this.loginToken = getLoginToken()
                login()
            } catch (ex: Exception) {
                this.loginToken = null
                this.infoToken = null
                throw AmplifiClientException("Failed to init Amplifi client session.")
            }
        }
    }

    override suspend fun getRouterMacAddress(): String {
        return ""
    }

    //    def get_router_mac_addr(self, devices):
//    for device in devices[0]:
//    if devices[0][device]["role"] == "Router":
//    return device
//
    override suspend fun getWanPortInfo(): String {
        return ""
    }

    override suspend fun testConnection(): Boolean {
        return false
    }
//    def get_wan_port_info(self, devices):
//    router_mac_addr = self.get_router_mac_addr(devices)
//    wan_port = devices[4][router_mac_addr]["eth-0"]
//    return wan_port
}