package com.mustafacellat.deneme

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo


@Suppress("DEPRECATION")
class Sayfa6 : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sayfa6)

        val text_canli = findViewById<View>(R.id.text_canli)

        val button = findViewById<Button>(R.id.button_forward)
        button.visibility = View.INVISIBLE

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            //val intent = Intent(this, Sayfa6::class.java)
            //startActivity(intent)
            text_canli.visibility = View.INVISIBLE
            button.visibility = View.VISIBLE

        }, 5000)

        button.setOnClickListener {
            val intent = Intent(this, Sayfa7::class.java)
            startActivity(intent)
        }
        YoYo.with(Techniques.FadeIn)
            .duration(1300)
            .repeat(YoYo.INFINITE)
            .playOn(text_canli)
    }

    override fun onBackPressed() {
        // Geri tuşuna basıldığında hiçbir şey yapma
    }
}
