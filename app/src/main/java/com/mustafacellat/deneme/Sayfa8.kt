package com.mustafacellat.deneme

import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class Sayfa8 : AppCompatActivity() {

    private lateinit var mediaPlayer: MediaPlayer

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sayfa8)

        mediaPlayer = MediaPlayer.create(this, R.raw.voice_deniz)

        mediaPlayer.setOnPreparedListener {
            mediaPlayer.start()
        }

        //val button = findViewById<Button>(R.id.button_giris)
        //button.visibility = View.INVISIBLE

        val button = findViewById<Button>(R.id.button_anasayfa)

        button.setOnClickListener {
            val intent = Intent(this, Sayfa2::class.java)
            startActivity(intent)
        }
    }
}