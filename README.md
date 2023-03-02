# Amplifi Client

Client for Amplifi devices (Ubiquiti).  It uses Ktor client under the hood and it is written as Kotlin Multiplatform.

## How to use:

```kotlin
// initialze ktor http client
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

// initialze amplifi client
val apiClient = AmplifiClient(client, "192.168.119.1", "somePassword")  

// get login token
val token = apiClient.getLoginToken()  
assertTrue("Login token must be present") { token.isNotBlank() }  
println("Login Token: $token")  

try {  
    apiClient.login()  
    print("Logged successfully")  
} catch (ex: Throwable) {  
    fail(ex.message)  
}  

// get info token
val infoToken = apiClient.getInfoToken()  
assertTrue("InfoToken must be present") { infoToken.isNotBlank() }  
println(infoToken)  

// get devices as List of Map
val devices: List<Map<String, JsonObject>> = apiClient.getDevices()  
assertTrue("devices is not present") { devices.isNotEmpty() }  
println(devices)

```

# Tasks to-do (Roadmap)

- [ ] Add more metods
- [ ] Study resonses and create models for it
- [ ] Publish to JitPack

# License

[Apache 2.0](./LICENSE.txt)
