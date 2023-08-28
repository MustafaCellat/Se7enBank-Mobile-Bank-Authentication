package com.mustafacellat.deneme

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class Sayfa3 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sayfa3)

        val button = findViewById<Button>(R.id.button_forward)
        button.setOnClickListener {
            val intent = Intent(this, Sayfa4::class.java)
            startActivity(intent)
        }
    }
}