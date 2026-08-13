package com.ejemplo.nfcapp

object NfcProtocol {
    // Nombre de preferencias compartidas usado por el emisor y el servicio HCE
    const val PREFS_NAME = "demo_nfc"
    const val PREF_MESSAGE = "mensaje"
    const val DEFAULT_MESSAGE = "Hola desde NFC real"

    private const val STATUS_OK = "9000"
    private const val STATUS_UNKNOWN = "0000"
    private const val STATUS_ERROR = "6F00"
    private const val SELECT_APDU = "00A4040007F001020304050600"
    private const val GET_MESSAGE_APDU = "00CA000000"

    val selectApdu: ByteArray = SELECT_APDU.hexToBytes()
    val getMessageApdu: ByteArray = GET_MESSAGE_APDU.hexToBytes()

    fun success(payload: ByteArray = ByteArray(0)): ByteArray = payload + STATUS_OK.hexToBytes()

    fun unknown(): ByteArray = STATUS_UNKNOWN.hexToBytes()

    fun error(): ByteArray = STATUS_ERROR.hexToBytes()

    fun isSelect(apdu: ByteArray): Boolean = apdu.contentEquals(selectApdu)

    fun isGetMessage(apdu: ByteArray): Boolean = apdu.contentEquals(getMessageApdu)

    fun parseSuccess(response: ByteArray): String {
        // Extrae el payload si la respuesta APDU es exitosa
        if (response.size < 2) return ""

        val status = response.takeLast(2).toByteArray()
        if (!status.contentEquals(STATUS_OK.hexToBytes())) return ""

        val payload = response.copyOfRange(0, response.size - 2)
        return String(payload, Charsets.UTF_8)
    }

    private fun String.hexToBytes(): ByteArray {
        return chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }
}
