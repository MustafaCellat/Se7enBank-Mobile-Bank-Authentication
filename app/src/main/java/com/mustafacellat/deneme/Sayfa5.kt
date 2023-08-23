package com.mustafacellat.deneme

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

class Sayfa5 : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sayfa5)

        Handler().postDelayed({
            val intent = Intent(this, Sayfa6::class.java)
            startActivity(intent)
            finish() // Sayfa 5'i kapat
        }, 5000)
    }
}