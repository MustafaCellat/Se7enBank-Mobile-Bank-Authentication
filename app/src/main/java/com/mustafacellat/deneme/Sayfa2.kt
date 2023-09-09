package com.mustafacellat.deneme

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Random

class Sayfa2 : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sayfa2)

        val button = findViewById<Button>(R.id.button_musteri)
        button.setOnClickListener {

            val uniqueValue = generateUniqueValue()
            val sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            with (sharedPref.edit()) {
                putString("uniqueValue", uniqueValue)
                apply()
            }

            sendUniqueToServer(uniqueValue)

            val intent = Intent(this, Sayfa3::class.java)
            startActivity(intent)
        }
    }

    private fun sendUniqueToServer(benzersiz: String) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val url = URL("https://sevenproject.azurewebsites.net/login") // Hedef endpointi güncelleyin
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true

                connection.setRequestProperty("unique", benzersiz)

                val outputStream: OutputStream = connection.outputStream
                outputStream.flush()

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    runOnUiThread {
                        // Handle response as needed
                    }
                } else {
                    runOnUiThread {
                        // Handle response error as needed
                    }
                }
                connection.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    // Handle exception as needed
                }
            }
        }
    }

    private fun generateUniqueValue(): String {
        val random = Random()
        val uniqueValue = random.nextInt(90000) + 10000
        return uniqueValue.toString()
    }

    override fun onBackPressed() {
        // Geri tuşuna basıldığında hiçbir şey yapma
    }
}