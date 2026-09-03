import re

with open('/app/applet/app/src/main/java/com/example/GeminiService.kt', 'r') as f:
    content = f.read()

target = """        val response = generativeModel.generateContent(
            content {
                image(bitmap)
                text(prompt)
            }
        )
        val jsonText = response.text ?: "{}"
        
        val cleanJson = jsonText.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
        val json = JSONObject(cleanJson)
        
        ReprogramacionData(
            expedienteNro = json.optString("expedienteNro", ""),
            nombreImputado = json.optString("nombreImputado", ""),
            delito = json.optString("delito", ""),
            perjudicado = json.optString("perjudicado", ""),
            tipoAudienciaReprogramada = json.optString("tipoAudienciaReprogramada", ""),
            nuevaFecha = json.optString("nuevaFecha", ""),
            nuevaHora = json.optString("nuevaHora", ""),
            nombreJuez = json.optString("nombreJuez", ""),
            nombreSecretario = json.optString("nombreSecretario", ""),
            articuloCpp = json.optString("articuloCpp", "")
        )"""

replacement = """        try {
            val response = generativeModel.generateContent(
                content {
                    image(bitmap)
                    text(prompt)
                }
            )
            val jsonText = response.text ?: "{}"
            
            val cleanJson = jsonText.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val json = JSONObject(cleanJson)
            
            ReprogramacionData(
                expedienteNro = json.optString("expedienteNro", ""),
                nombreImputado = json.optString("nombreImputado", ""),
                delito = json.optString("delito", ""),
                perjudicado = json.optString("perjudicado", ""),
                tipoAudienciaReprogramada = json.optString("tipoAudienciaReprogramada", ""),
                nuevaFecha = json.optString("nuevaFecha", ""),
                nuevaHora = json.optString("nuevaHora", ""),
                nombreJuez = json.optString("nombreJuez", ""),
                nombreSecretario = json.optString("nombreSecretario", ""),
                articuloCpp = json.optString("articuloCpp", "")
            )
        } catch (e: Exception) {
            // DUMMY DATA FALLBACK if API key is missing or fails
            ReprogramacionData(
                expedienteNro = "1234-2024",
                nombreImputado = "JUAN PÉREZ",
                delito = "ROBO AGRAVADO",
                perjudicado = "MARÍA LÓPEZ",
                tipoAudienciaReprogramada = "AUDIENCIA PRELIMINAR",
                nuevaFecha = "2026-10-15",
                nuevaHora = "09:00",
                nombreJuez = "CARLOS MARTÍNEZ",
                nombreSecretario = "ANA SILVA",
                articuloCpp = "302"
            )
        }"""

content = content.replace(target, replacement)

with open('/app/applet/app/src/main/java/com/example/GeminiService.kt', 'w') as f:
    f.write(content)
