package com.ejemplo.nfcapp

import android.nfc.NfcAdapter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor

class EmisorActivity : AppCompatActivity() {

    private lateinit var etMensaje: EditText
    private lateinit var tvEstado: TextView

    private lateinit var layoutTarjeta: LinearLayout
    private lateinit var layoutExito: LinearLayout
    private lateinit var layoutError: LinearLayout

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emisor)

        etMensaje = findViewById(R.id.etMensaje)
        tvEstado = findViewById(R.id.tvEstado)

        layoutTarjeta = findViewById(R.id.layoutTarjeta)
        layoutExito = findViewById(R.id.layoutExito)
        layoutError = findViewById(R.id.layoutError)

        mostrarEstadoNfc()

        executor = ContextCompat.getMainExecutor(this)
        configurarBiometria()

        findViewById<Button>(R.id.btnSimular).setOnClickListener {
            val biometricManager = BiometricManager.from(this)
            val estadoBiometrico = biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )

            if (estadoBiometrico == BiometricManager.BIOMETRIC_SUCCESS) {
                biometricPrompt.authenticate(promptInfo)
            } else {
                Toast.makeText(this, "Modo Demo: Sin biometría disponible", Toast.LENGTH_SHORT).show()
                animarResultado(exito = true)
            }
        }
    }

    private fun configurarBiometria() {
        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Verificación de Seguridad")
            .setSubtitle("Use su huella o rostro para autorizar el acceso")
            .setNegativeButtonText("Cancelar")
            .build()

        biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Toast.makeText(applicationContext, "Acceso Authorized", Toast.LENGTH_SHORT).show()
                animarResultado(exito = true)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Toast.makeText(applicationContext, "Error: $errString", Toast.LENGTH_SHORT).show()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(applicationContext, "Huella no reconocida", Toast.LENGTH_SHORT).show()
                animarResultado(exito = false)
            }
        })
    }

    private fun mostrarEstadoNfc() {
        val adapter = NfcAdapter.getDefaultAdapter(this)
        tvEstado.text = when {
            adapter == null -> "Dispositivo sin NFC físico (Modo Demo)."
            !adapter.isEnabled -> "NFC desactivado. Actívalo en ajustes."
            else -> "Tarjeta activa. Acércala al lector de la puerta."
        }
    }

    private fun animarResultado(exito: Boolean) {
        layoutTarjeta.visibility = View.GONE

        if (exito) {
            layoutExito.visibility = View.VISIBLE
        } else {
            layoutError.visibility = View.VISIBLE
        }

        Handler(Looper.getMainLooper()).postDelayed({
            layoutExito.visibility = View.GONE
            layoutError.visibility = View.GONE
            layoutTarjeta.visibility = View.VISIBLE
        }, 2500)
    }
}
