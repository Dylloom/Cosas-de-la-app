package com.ejemplo.nfcapp

import android.app.Activity
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Bundle
import android.widget.TextView
import java.io.IOException

class ReceptorActivity : Activity(), NfcAdapter.ReaderCallback {

    private lateinit var tvEstado: TextView
    private lateinit var tvMensaje: TextView
    private var nfcAdapter: NfcAdapter? = null

    // Esta actividad muestra el estado de lectura NFC y procesa el mensaje recibido

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receptor)

        tvEstado = findViewById(R.id.tvEstado)
        tvMensaje = findViewById(R.id.tvMensaje)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        // Procesa el Intent si llega un mensaje NDEF simulado
        procesarIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        activarLecturaReal()
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableReaderMode(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        procesarIntent(intent)
    }

    override fun onTagDiscovered(tag: Tag) {
        // Lee tarjetas NFC compatibles con IsoDep desde un emisor HCE
        val isoDep = IsoDep.get(tag)
        if (isoDep == null) {
            mostrarResultado("Tag detectado, pero no es compatible con IsoDep.", "")
            return
        }

        try {
            isoDep.use {
                it.connect()
                it.timeout = 3000

                it.transceive(NfcProtocol.selectApdu)
                val response = it.transceive(NfcProtocol.getMessageApdu)
                val mensaje = NfcProtocol.parseSuccess(response)

                if (mensaje.isBlank()) {
                    mostrarResultado("No se pudo leer un mensaje NFC valido.", "")
                } else {
                    mostrarResultado("Mensaje recibido por NFC real", mensaje)
                }
            }
        } catch (error: IOException) {
            mostrarResultado(
                "Acerca los telefonos otra vez y mantenlos quietos.",
                error.message.orEmpty()
            )
        }
    }

    private fun activarLecturaReal() {
        // Activa el modo lector NFC para recibir mensajes reales de HCE
        val adapter = nfcAdapter
        if (adapter == null) {
            tvEstado.text = "Este dispositivo no tiene NFC."
            return
        }

        if (!adapter.isEnabled) {
            tvEstado.text = "Activa NFC en ajustes para leer otro celular."
            return
        }

        tvEstado.text = "Listo. Acerca este telefono al emisor."
        adapter.enableReaderMode(
            this,
            this,
            NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
            null
        )
    }

    private fun mostrarResultado(estado: String, mensaje: String) {
        runOnUiThread {
            tvEstado.text = estado
            tvMensaje.text = mensaje
        }
    }

    private fun procesarIntent(intent: Intent) {
        // Procesa mensajes NDEF recibidos en el Intent
        val rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES, NdefMessage::class.java)

        if (rawMessages.isNullOrEmpty()) {
            tvMensaje.text = ""
            return
        }

        val mensajeCompleto = rawMessages
            .mapNotNull { it as? NdefMessage }
            .flatMap { it.records.toList() }
            .mapNotNull(::leerTexto)
            .joinToString(separator = "\n")

        tvEstado.text = "Mensaje recibido por simulacion"
        tvMensaje.text = mensajeCompleto.ifBlank { "El mensaje NFC no contiene texto legible." }
    }

    private fun leerTexto(record: NdefRecord): String? {
        if (record.tnf != NdefRecord.TNF_WELL_KNOWN ||
            !record.type.contentEquals(NdefRecord.RTD_TEXT)
        ) {
            return null
        }

        val payload = record.payload
        if (payload.isEmpty()) return null

        val idiomaLength = payload[0].toInt() and 0x3F
        val textoInicio = idiomaLength + 1
        if (textoInicio > payload.size) return null

        return String(
            payload,
            textoInicio,
            payload.size - textoInicio,
            Charsets.UTF_8
        )
    }
}
