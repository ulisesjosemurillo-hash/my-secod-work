import java.util.regex.Regex

val simulatedOcrText = """
    Juzgado de Letras Penal
    EXPEDIENTE N° 12345-2026
    Causa instruida contra el encausado CARLOS MANUEL MARTINEZ TRIGUEROS, a quien se le supone responsable del delito de TRAFICO DE DROGA EN SU MODALIDAD DE POSESION en perjuicio de la SALUD PUBLICA DEL ESTADO DE HONDURAS.
    Se señala AUDIENCIA INICIAL para el día 20 de mayo de 2026 a las 09:00 a.m.
    De conformidad con el Artículo 123 del Código Procesal Penal.
    JUEZ: ABOG. JORGE ALBERTO FLORES
    SECRETARIO: MARIO RENE CRUZ
""".trimIndent()

fun extractExpediente(text: String): String {
    val regexes = listOf(
        Regex("(?i)(?:EXP(?:EDIENTE|\\.)?|CAUSA|N[°º])\\s*(?:No\\.?|N[°º]\\.?)?\\s*([0-9]+-[0-9]{4}(?:-[0-9]+)?|[0-9]{4,5}-[0-9]{4})"),
        Regex("(?i)([0-9]{4,5}-[0-9]{4})")
    )
    for (regex in regexes) {
        val match = regex.find(text)
        if (match != null) return match.groupValues[1].trim()
    }
    return "NO IDENTIFICADO"
}

fun extractImputado(text: String): String {
    val cleanText = text.replace(Regex("\\s+"), " ")
    val regexes = listOf(
        Regex("(?i)contra(?: el)? (?:encausado|imputado|acusado)?\\s*([A-ZÑÁÉÍÓÚ ]+?)\\s*(?:, a quien|a quien|por el delito|,|\\.)"),
        Regex("(?i)causa instruida contra\\s*([A-ZÑÁÉÍÓÚ ]+?)\\s*(?:a quien|por el|,|\\.)"),
        Regex("(?i)(?:encausado|imputado|acusado)(?:s)?:?\\s*([A-ZÑÁÉÍÓÚ ]+?)\\s*(?:a quien|por el|,|\\.|\\n)")
    )
    for (regex in regexes) {
        val match = regex.find(cleanText)
        if (match != null) {
            var name = match.groupValues[1].trim()
            val badEndings = listOf(" A QUIEN", " POR EL", " DEL DELITO")
            for (bad in badEndings) {
                val idx = name.uppercase().indexOf(bad)
                if (idx != -1) name = name.substring(0, idx)
            }
            return name.trim()
        }
    }
    return "NO IDENTIFICADO"
}

fun extractDelito(text: String): String {
    val cleanText = text.replace(Regex("\\s+"), " ")
    val regexes = listOf(
        Regex("(?i)(?:delito de|responsable del delito de|por el delito de)\\s+([A-ZÑÁÉÍÓÚ ]+?)\\s*(?:en perjuicio|,|\\.|\\n)"),
        Regex("(?i)delito(?:s)?:?\\s*([A-ZÑÁÉÍÓÚ ]+?)\\s*(?:en perjuicio|,|\\.|\\n)")
    )
    for (regex in regexes) {
        val match = regex.find(cleanText)
        if (match != null) return match.groupValues[1].trim()
    }
    return "NO IDENTIFICADO"
}

fun extractPerjudicado(text: String): String {
    val cleanText = text.replace(Regex("\\s+"), " ")
    val regexes = listOf(
        Regex("(?i)en perjuicio de(?:l| la)?\\s+([A-ZÑÁÉÍÓÚ ]+?)\\s*(?:\\.|,|\\n|y)"),
        Regex("(?i)en perjuicio del Estado\\b")
    )
    for (regex in regexes) {
        val match = regex.find(cleanText)
        if (match != null) {
            if (match.value.lowercase().contains("estado")) return "EL ESTADO DE HONDURAS"
            return match.groupValues[1].trim()
        }
    }
    return "NO IDENTIFICADO"
}

fun extractTipoAudiencia(text: String): String {
    val tipos = listOf(
        "AUDIENCIA INICIAL", "AUDIENCIA PRELIMINAR", "AUDIENCIA DE IMPOSICIÓN DE MEDIDAS",
        "AUDIENCIA DE REVISIÓN DE MEDIDAS", "AUDIENCIA DE PROCEDIMIENTO ABREVIADO",
        "AUDIENCIA DE JUICIO ORAL Y PÚBLICO", "AUDIENCIA DE CONCILIACIÓN",
        "AUDIENCIA DE DECLARACIÓN DE IMPUTADO", "JUICIO ORAL Y PÚBLICO"
    )
    val upperText = text.uppercase()
    for (tipo in tipos) {
        if (upperText.contains(tipo)) return tipo
    }
    val fallbackRegex = Regex("(?i)audiencia\\s+([a-zñáéíóú ]+?)\\s*(?:para el|señalada|\\n|,)")
    val match = fallbackRegex.find(text)
    return match?.groupValues?.get(1)?.trim()?.uppercase() ?: "NO IDENTIFICADO"
}

fun extractFecha(text: String): String {
    val regexes = listOf(
        Regex("(?i)([0-9]{1,2})\\s+de\\s+([a-zñ]+)\\s+d(?:e|el)(?:\\s+a[ñn]o)?\\s+((?:dos mil\\s+[a-z]+|[0-9]{4}))"),
        Regex("(?i)([0-9]{1,2})/[0-9]{1,2}/([0-9]{4})"),
        Regex("(?i)([0-9]{1,2})-[0-9]{1,2}-([0-9]{4})")
    )
    for (regex in regexes) {
        val match = regex.find(text)
        if (match != null) return match.value.trim()
    }
    return "NO IDENTIFICADO"
}

fun extractHora(text: String): String {
    val regexes = listOf(
        Regex("(?i)([0-9]{1,2}):([0-9]{2})\\s*(a\\.?m\\.?|p\\.?m\\.?)?"),
        Regex("(?i)([0-9]{1,2})\\s*:\\s*([0-9]{2})"),
        Regex("(?i)(un|dos|tres|cuatro|cinco|seis|siete|ocho|nueve|diez|once|doce)\\s+(?:con\\s+[a-z]+\\s+minutos\\s+)?de la (mañana|tarde)")
    )
    for (regex in regexes) {
        val match = regex.find(text)
        if (match != null) return match.value.trim()
    }
    return "NO IDENTIFICADO"
}
    
fun extractArticulo(text: String): String {
    val regexes = listOf(
        Regex("(?i)Art[íi]culo[s]?\\s+([0-9]+(?:\\s*,\\s*[0-9]+)*)(?:\\s*del\\s*C[óo]digo|\\s*CPP)?"),
        Regex("(?i)Art\\.\\s*([0-9]+(?:\\s*,\\s*[0-9]+)*)")
    )
    for (regex in regexes) {
        val match = regex.find(text)
        if (match != null) return match.groupValues[1].trim()
    }
    return "NO IDENTIFICADO"
}

fun extractJuez(text: String): String {
    val regexes = listOf(
        Regex("(?i)(?:JUEZ|JUEZA):?\\s*([A-ZÑÁÉÍÓÚ ]+?)\\s*(?:\\n|$)"),
        Regex("(?i)(?:ABOG\\.|ABOGADA|ABOGADO)\\s+([A-ZÑÁÉÍÓÚ ]+?)\\s*(?:\\n|$)")
    )
    val lastPart = text.takeLast(500)
    for (regex in regexes) {
        val match = regex.find(lastPart)
        if (match != null) {
            val name = match.groupValues[1].trim()
            if(name.length < 50) return name
        }
    }
    return "NO IDENTIFICADO"
}

fun extractSecretario(text: String): String {
    val regexes = listOf(
        Regex("(?i)(?:SECRETARIO|SECRETARIA|SECRETARIO\\(A\\) ADJUNTO\\(A\\)):?\\s*([A-ZÑÁÉÍÓÚ ]+?)\\s*(?:\\n|$)")
    )
    val lastPart = text.takeLast(500)
    for (regex in regexes) {
        val match = regex.find(lastPart)
        if (match != null) {
            val name = match.groupValues[1].trim()
            if(name.length < 50) return name
        }
    }
    return "NO IDENTIFICADO"
}

println("=== RESULTADOS DE EXTRACCION ===")
println("Expediente: " + extractExpediente(simulatedOcrText))
println("Imputado: " + extractImputado(simulatedOcrText))
println("Delito: " + extractDelito(simulatedOcrText))
println("Perjudicado: " + extractPerjudicado(simulatedOcrText))
println("Tipo Audiencia: " + extractTipoAudiencia(simulatedOcrText))
println("Fecha Original: " + extractFecha(simulatedOcrText))
println("Hora Original: " + extractHora(simulatedOcrText))
println("Articulo: " + extractArticulo(simulatedOcrText))
println("Juez: " + extractJuez(simulatedOcrText))
println("Secretario: " + extractSecretario(simulatedOcrText))
