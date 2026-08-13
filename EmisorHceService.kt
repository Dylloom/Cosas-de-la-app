package com.ejemplo.nfcapp

import android.nfc.cardemulation.HostApduService
import android.os.Bundle

class EmisorHceService : HostApduService() {

    // Servicio HCE que responde a comandos APDU desde el receptor NFC
    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
        if (commandApdu == null) return NfcProtocol.error()

        return when {
            NfcProtocol.isSelect(commandApdu) -> NfcProtocol.success()
            NfcProtocol.isGetMessage(commandApdu) -> {
                // Devuelve el mensaje guardado al lector NFC mediante HCE
                val mensaje = getSharedPreferences(NfcProtocol.PREFS_NAME, MODE_PRIVATE)
                    .getString(NfcProtocol.PREF_MESSAGE, NfcProtocol.DEFAULT_MESSAGE)
                    .orEmpty()

                NfcProtocol.success(mensaje.toByteArray(Charsets.UTF_8))
            }
            else -> NfcProtocol.unknown()
        }
    }

    override fun onDeactivated(reason: Int) = Unit
}
