package com.example

import org.junit.Test
import org.junit.Assert.*

class DataExtractorTest {
    @Test
    fun testExtraction() {
        val simulatedOcrText = """
            Juzgado de Letras Penal
            EXPEDIENTE N° 12345-2026
            Causa instruida contra el encausado CARLOS MANUEL MARTINEZ TRIGUEROS, a quien se le supone responsable del delito de TRAFICO DE DROGA EN SU MODALIDAD DE POSESION en perjuicio de la SALUD PUBLICA DEL ESTADO DE HONDURAS.
            Se señala AUDIENCIA INICIAL para el día 20 de mayo de 2026 a las 09:00 a.m.
            De conformidad con el Artículo 123 del Código Procesal Penal.
            JUEZ: ABOG. JORGE ALBERTO FLORES
            SECRETARIO: MARIO RENE CRUZ
        """.trimIndent()
        
        val result = DataExtractor.parseOcrResult(simulatedOcrText)
        println("=== RESULTADOS DE EXTRACCION ===")
        println("Expediente: " + result.expedienteNro)
        println("Imputado: " + result.nombreImputado)
        println("Delito: " + result.delito)
        println("Perjudicado: " + result.perjudicado)
        println("Tipo Audiencia: " + result.tipoAudienciaReprogramada)
        println("Fecha Original: " + result.fechaOriginal)
        println("Hora Original: " + result.horaOriginal)
        println("Articulo: " + result.articuloCpp)
        println("Juez: " + result.nombreJuez)
        println("Secretario: " + result.nombreSecretario)
    }
}
