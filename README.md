# API Gateway SDK Integration

## Changelog (v2.0.1)

- Bug fix - New generated bind-client-config.json not working

## Changelog (v1.0.0)
- Initial integration of the API Gateway SDK.
- Supports **Retrofit** and **OkHttp** only.

---

## Onboarding Process
1. Send email to `apigw@technonext.com` to get `bind_client_config.json`.
2. Place `bind_client_config.json` in the **root directory** of your project.
3. Get the SDK `.aar` file from `Sample Project` and place it in your project's `libs` folder.
4. Follow the Gradle and configuration steps as described below.
5. Add  `ApiGateWayInterceptor` at the bottom of your OkHttp client interceptor chain.

---

## Project Setup

### Root `build.gradle.kts`
Add the Mapnests `config-loader` plugin.

Plugin: `com.mapnests.config-loader:com.mapnests.config-loader.gradle.plugin:2.0.0`

``` groovy
buildscript {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.13.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.21")
        classpath("com.mapnests.config-loader:com.mapnests.config-loader.gradle.plugin:2.0.0")
    }
}
```

### Module build.gradle

Set Java and Kotlin compatibility:

```groovy
plugins {
  // other gradle plugins
  id("com.mapnests.config-loader")
}

compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}
```

Add SDK libraries from the libs folder:
```gradle
dependencies {
  implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))
  
  // okhttp 
  implementation("com.squareup.okhttp3:okhttp:5.3.2")
  implementation("com.squareup.okhttp3:logging-interceptor:5.3.2")
  // Retrofit core
  implementation("com.squareup.retrofit2:retrofit:3.0.0")
  implementation("com.squareup.retrofit2:converter-gson:3.0.0")
      
  implementation("com.lyft.kronos:kronos-android:0.0.1-alpha11")
}
```

### Configuration

- Place bind_client_config.json in the root of your project.
- Ensure the app_id in JSON matches the application ID in Gradle.
- Add network permission in AndroidManifest.xml:
    ```
    <uses-permission android:name="android.permission.INTERNET"/>
    ```
- Only use HTTP if necessary. In that case enable cleartext traffic.
- Supports **Retrofit** and **OkHttp** only. For other clients, kindly contact support us at `apigw@technonext.com`.

---

## SDK Integration

- Add ApiGateWayInterceptor at the bottom of your OkHttp client interceptor chain:
```kotlin
val okHttpClient = OkHttpClient.Builder()
    // other interceptors
    .addInterceptor(ApiGateWayInterceptor())
    .build()
```

### PROJECT MUST BE GRADLE CLEAN AND GRADLE SYNCED BEFORE USE

---

## Implementation Example with Retrofit

**Note: Must use correct URL (consult with api gateway team if needed)**

```kotlin
fun performApiCall(context: Context, onLogUpdate: (String) -> Unit) {
    val retrofit = create("http://example.baseURL/api/", context)
    val apiService = retrofit.create(ApiService::class.java)
    val call = apiService.getRandomDog()

    val startTime = System.currentTimeMillis()
    val startTimeFormatted = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        .format(Date(startTime))

    // Initial log
    val request = call.request()

    onLogUpdate(
        "Request started at: $startTimeFormatted\n" +
                "Request URL: ${request.url}\n" +
                "Request Method: ${request.method}\n" +
                "Waiting for response..."
    )

    call.enqueue(object : Callback<ResponseBody?> {
        override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
            val endTime = System.currentTimeMillis()
            val duration = endTime - startTime

            val requestHeaders = response.raw().request.headers.toMultimap()
                .entries.joinToString("\n") { (key, values) -> "$key: ${values.joinToString(", ")}" }

            val bodyText = response.body()?.string() ?: response.errorBody()?.string() ?: "No body"

            val logText = "Request started at: $startTimeFormatted\n" +
                    "Duration: ${duration}ms\n\n" +
                    "Request URL: ${response.raw().request.url}\n" +
                    "Request Method: ${response.raw().request.method}\n\n" +
                    "Request Headers:\n$requestHeaders\n\n" +
                    "Response Body:\n$bodyText"

            Log.d("MainActivity", logText)
            onLogUpdate(logText)
        }

        override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
            val endTime = System.currentTimeMillis()
            val duration = endTime - startTime
            val sentRequestHeaders = call.request().headers.toMultimap()
                .entries.joinToString("\n") { (key, values) -> "$key: ${values.joinToString(", ")}" }

            val logText = "Request started at: $startTimeFormatted\n" +
                    "Duration: ${duration}ms\n\n" +
                    "Request URL: ${call.request().url}\n" +
                    "Request Method: ${call.request().method}\n\n" +
                    "Request Headers:\n$sentRequestHeaders\n\n" +
                    "Failure: ${t.message}"

            Log.e("MainActivity", logText)
            onLogUpdate(logText)
        }
    })
}
```

---

## Request
```json
{
  "method": "GET",
  "url": "http://192.168.61.103:9080/load-test/api/auth-casbin-success-plugin-test",
  "timestamp": "2025-12-07 15:51:32.497",
  "headers": {
    "Client-Header-Name1": "xxxxxx",
    "Client-Header-Name2": "yyyyyy",
    "x-client-identity": "KlD0r8ocjAqEbzeEqiQYeycvIKuKimA6btcfgGqUcDo1DXOBR8M+z6XKwa/uP0ee1H4Di53awaeZrh8vsbd6H0RSTs+By0cmSl6XROCpupDPdPfT78cCchjF+LC7oCFbnztVAPXKhTercr2zcRE7uLQA1Yfmx7xbinYGrFCud0fZbdqYWLp6i9sycXQjvVFpw7G2bz2x2IWNY/SzhWuSU31rnjpZMdI0RLNy/zUu2awj5LBmO0zk0cRYTnhWnZLCmbCiuu0I+Ag6UJm/H9hqJecB59NmTyqWUzRK/tUNnpcQhC2WGneyb9gAa25mfQ1xcYYL7WWk5Xc1ci5nx3/alQ==",
    "x-key-identity": "596119440572023487"
  },
  "request_id": "1765101092497"
}
```

## Response
```json
{
  "status": 200,
  "timestamp": "2025-12-07 15:51:32.673",
  "headers": {
    "Connection": "keep-alive",
    "Content-Type": "application/json",
    "Date": "Sun, 07 Dec 2025 09:51:31 GMT",
    "Server": "APISIX/3.14.1",
    "Transfer-Encoding": "chunked"
  },
  "body": {
    "message": "Auth Casbin plugin test api call succeed"
  },
  "response_id": "177"
}
```

## Developer Notes
- Keep package names consistent between JSON and Gradle.
- SDK currently supports **Retrofit** and **OkHttp** only.
- **Memory management** and **thread safety** are the developer's responsibility.
- HTTPS is preferred. Only enable HTTP with cleartext traffic if necessary.
- To update version, change it in **build.gradle.kts**:
  `classpath("com.mapnests.config-loader:com.mapnests.config-loader.gradle.plugin:2.0.0")`

---

## Common Fixes

- Invalidate Caches from android studio (check "clear file system cache")
- Delete Build files
- Gradle Clean and Sync project

## Support

For issues or feature requests contact us through email: `apigw@technonext.com`

---