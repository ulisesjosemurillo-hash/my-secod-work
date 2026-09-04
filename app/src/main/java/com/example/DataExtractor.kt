package com.example

object DataExtractor {

    fun normalizeText(text: String): String {
        return text
            .replace(Regex("""\r?\n"""), " ")
            .replace(Regex("""\s+"""), " ")
            .trim()
    }

    fun extractExpediente(text: String): String {
        val cleanText = normalizeText(text)

        val regexes = listOf(
            Regex(
                """(?i)(?:EXP(?:EDIENTE|\.)?|CAUSA|N[°º])\s*(?:No\.?|N[°º]\.?)?\s*([0-9]+-[0-9]{4}(?:-[0-9]+)?|[0-9]{4,5}-[0-9]{4})"""
            ),
            Regex("""(?i)([0-9]{4,5}-[0-9]{4})""")
        )

        for (regex in regexes) {
            val match = regex.find(cleanText)
            if (match != null) {
                return match.groupValues[1].trim()
            }
        }

        return "NO IDENTIFICADO"
    }

    fun extractImputado(text: String): String {
        val cleanText = normalizeText(text)

        val regexes = listOf(
            Regex(
                """(?i)contra(?: el)? (?:encausado|imputado|acusado)?\s*([A-ZÑÁÉÍÓÚ ]+?)\s*(?:, a quien|a quien|por el delito|,|\.)"""
            ),
            Regex(
                """(?i)causa instruida contra\s*([A-ZÑÁÉÍÓÚ ]+?)\s*(?:a quien|por el|,|\.)"""
            ),
            Regex(
                """(?i)(?:encausado|imputado|acusado)(?:s)?:?\s*([A-ZÑÁÉÍÓÚ ]+?)\s*(?:a quien|por el|,|\.|$)"""
            )
        )

        for (regex in regexes) {
            val match = regex.find(cleanText)

            if (match != null) {
                var name = match.groupValues[1].trim()

                val badEndings = listOf(
                    " A QUIEN",
                    " POR EL",
                    " DEL DELITO"
                )

                for (bad in badEndings) {
                    val idx = name.uppercase().indexOf(bad)

                    if (idx != -1) {
                        name = name.substring(0, idx)
                    }
                }

                return name.trim()
            }
        }

        return "NO IDENTIFICADO"
    }

    fun extractDelito(text: String): String {
        val cleanText = normalizeText(text)

        val regexes = listOf(
            Regex(
                """(?i)(?:delito de|responsable del delito de|por el delito de)\s+([A-ZÑÁÉÍÓÚ ]+?)\s*(?:en perjuicio|,|\.|$)"""
            ),
            Regex(
                """(?i)delito(?:s)?:?\s*([A-ZÑÁÉÍÓÚ ]+?)\s*(?:en perjuicio|,|\.|$)"""
            )
        )

        for (regex in regexes) {
            val match = regex.find(cleanText)

            if (match != null) {
                return match.groupValues[1].trim()
            }
        }

        return "NO IDENTIFICADO"
    }

    fun extractPerjudicado(text: String): String {
        val cleanText = normalizeText(text)

        val regexes = listOf(
            Regex(
                """(?i)en perjuicio de(?:l| la)?\s+([A-ZÑÁÉÍÓÚ\s]+?)(?:;|\.|-|para el d[ií]a|a quien se le|orden[áa]ndosele|Art[íi]culo|$)"""
            )
        )

        for (regex in regexes) {
            val match = regex.find(cleanText)

            if (match != null) {
                return match.groupValues[1].trim()
            }
        }

        return "NO IDENTIFICADO"
    }

    fun extractTipoAudiencia(text: String): String {
        val cleanText = normalizeText(text)

        val tipos = listOf(
            "AUDIENCIA INICIAL",
            "AUDIENCIA PRELIMINAR",
            "AUDIENCIA DE IMPOSICIÓN DE MEDIDAS",
            "AUDIENCIA DE REVISIÓN DE MEDIDAS",
            "AUDIENCIA DE PROCEDIMIENTO ABREVIADO",
            "AUDIENCIA DE JUICIO ORAL Y PÚBLICO",
            "AUDIENCIA DE CONCILIACIÓN",
            "AUDIENCIA DE DECLARACIÓN DE IMPUTADO",
            "JUICIO ORAL Y PÚBLICO"
        )

        val upperText = cleanText.uppercase()

        for (tipo in tipos) {
            if (upperText.contains(tipo)) {
                return tipo
            }
        }

        val fallbackRegex = Regex(
            """(?i)audiencia\s+([a-zñáéíóú ]+?)\s*(?:para el|señalada|,|$)"""
        )

        val match = fallbackRegex.find(cleanText)

        return match?.groupValues?.get(1)?.trim()?.uppercase()
            ?: "NO IDENTIFICADO"
    }

    fun extractArticulo(text: String): String {
        val cleanText = normalizeText(text)

        val regexes = listOf(
            Regex(
                """(?i)Art[íi]culo[s]?\s+([0-9]+(?:\s*,\s*[0-9]+)*)(?:\s*del\s*C[óo]digo|\s*CPP)?"""
            ),
            Regex(
                """(?i)Art\.\s*([0-9]+(?:\s*,\s*[0-9]+)*)"""
            )
        )

        for (regex in regexes) {
            val match = regex.find(cleanText)

            if (match != null) {
                return match.groupValues[1].trim()
            }
        }

        return "NO IDENTIFICADO"
    }

    fun parseOcrResult(text: String): ReprogramacionData {
        return ReprogramacionData(
            expedienteNro = extractExpediente(text),
            nombreImputado = extractImputado(text),
            delito = extractDelito(text),
            perjudicado = extractPerjudicado(text),
            tipoAudienciaReprogramada = extractTipoAudiencia(text),

            // Estos dos datos ya NO vienen del OCR.
            // El usuario los seleccionará manualmente.
            nombreJuez = "",
            nombreSecretario = "",

            articuloCpp = extractArticulo(text),

            // Se introducirá manualmente.
            motivoReprogramacion = "",

            // También se seleccionarán manualmente.
            nuevaFecha = "",
            nuevaHora = ""
        )
    }
}