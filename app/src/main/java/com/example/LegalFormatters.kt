package com.example

object LegalFormatters {

    private val UNIDADES = arrayOf("", "UNO", "DOS", "TRES", "CUATRO", "CINCO", "SEIS", "SIETE", "OCHO", "NUEVE")
    private val DECENAS = arrayOf("", "DIEZ", "VEINTE", "TREINTA", "CUARENTA", "CINCUENTA")
    private val ESPECIALES = arrayOf("DIEZ", "ONCE", "DOCE", "TRECE", "CATORCE", "QUINCE", "DIECISEIS", "DIECISIETE", "DIECIOCHO", "DIECINUEVE")
    private val VEINTIS = arrayOf("VEINTE", "VEINTIUNO", "VEINTIDOS", "VEINTITRES", "VEINTICUATRO", "VEINTICINCO", "VEINTISEIS", "VEINTISIETE", "VEINTIOCHO", "VEINTINUEVE")

    private val MESES = arrayOf("", "ENERO", "FEBRERO", "MARZO", "ABRIL", "MAYO", "JUNIO", "JULIO", "AGOSTO", "SEPTIEMBRE", "OCTUBRE", "NOVIEMBRE", "DICIEMBRE")

    fun numeroALetras(num: Int): String {
        if (num == 0) return "CERO"
        if (num in 1..9) return UNIDADES[num]
        if (num in 10..19) return ESPECIALES[num - 10]
        if (num in 20..29) return VEINTIS[num - 20]
        if (num in 30..31) return if (num == 30) "TREINTA" else "TREINTA Y UNO"
        if (num in 40..59) {
            val d = num / 10
            val u = num % 10
            return DECENAS[d] + (if (u > 0) " Y ${UNIDADES[u]}" else "")
        }
        if (num in 2000..2999) {
            val resto = num % 2000
            if (resto == 0) return "DOS MIL"
            var restStr = ""
            if (resto in 1..9) restStr = UNIDADES[resto]
            else if (resto in 10..19) restStr = ESPECIALES[resto - 10]
            else if (resto in 20..29) restStr = VEINTIS[resto - 20]
            else if (resto in 30..99) {
                val d = resto / 10
                val u = resto % 10
                restStr = DECENAS[d] + (if (u > 0) " Y ${UNIDADES[u]}" else "")
            } else if (resto >= 100) {
                // simple hack for years up to 2099
            }
            return "DOS MIL $restStr".trim()
        }
        return num.toString()
    }
    
    fun mesALetras(mes: Int): String {
        return if (mes in 1..12) MESES[mes] else ""
    }

    fun horaALetrasYNumeros(timeString: String): String {
        try {
            val parts = timeString.split(":")
            if (parts.size != 2) return timeString
            var h = parts[0].toInt()
            val m = parts[1].toInt()
            val ampm = if (h < 12) "a. m." else "p. m."
            val periodo = when {
                h == 12 && m == 0 -> "DEL MEDIODÍA"
                h == 0 && m == 0 -> "DE LA MEDIANOCHE"
                h < 12 -> "DE LA MAÑANA"
                h in 12..18 -> "DE LA TARDE"
                else -> "DE LA NOCHE"
            }
            
            val h12 = if (h % 12 == 0) 12 else h % 12
            val hLetras = numeroALetras(h12)
            
            val base = if (m == 0) {
                "$hLetras EN PUNTO $periodo"
            } else {
                val mLetras = numeroALetras(m)
                "$hLetras CON $mLetras MINUTOS $periodo"
            }
            return "$base ($h12:${parts[1]} $ampm)"
        } catch (e: Exception) {
            return timeString
        }
    }

    fun toUpperCaseKeepAccents(text: String): String {
        return text.uppercase()
    }
}
