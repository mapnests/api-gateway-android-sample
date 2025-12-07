package com.sdk.apigatewaysample

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sdk.apigatewaysample.RetrofitClient.create
import com.sdk.apigatewaysample.ui.theme.APIGatewaySampleTheme
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            APIGatewaySampleTheme {
                MainScreen(context = applicationContext)
            }
        }
    }
}

@Composable
fun MainScreen(context: Context) {
    var apiLog by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                ApiCallButton {
                    performApiCall(context) { logText ->
                        apiLog = logText
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                ScrollableLogBox(logText = apiLog, scrollState = scrollState)
            }
        }
    }
}

@Composable
fun ApiCallButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium
    ) {
        Text(text = "Call me")
    }
}

@Composable
fun ScrollableLogBox(logText: String, scrollState: androidx.compose.foundation.ScrollState) {
    Column(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .height(400.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = MaterialTheme.shapes.small
            )
            .padding(12.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = logText.ifEmpty { "Press \"Call me\" button to get API response" },
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Start
        )
    }
}


fun performApiCall(context: Context, onLogUpdate: (String) -> Unit) {
    val retrofit = create("http://192.168.61.103:9080/", context)
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

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    APIGatewaySampleTheme {
        MainScreen(context = androidx.compose.ui.platform.LocalContext.current)
    }
}