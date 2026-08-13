package com.ejemplo.nfcapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Al presionar el botón, nos lleva directo a la tarjeta digital (EmisorActivity)
        findViewById<Button>(R.id.btnAbrirPuerta).setOnClickListener {
            startActivity(Intent(this, EmisorActivity::class.java))
        }
    }
}
