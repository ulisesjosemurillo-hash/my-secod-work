package com.example

import java.time.LocalDate

object DocumentGenerator {
    fun generate(data: ReprogramacionData, isConstancia: Boolean): String {
        val today = LocalDate.now()
        val dLiteral = today.dayOfMonth.toString()
        val dLetras = LegalFormatters.numeroALetras(today.dayOfMonth)
        val mLiteral = LegalFormatters.mesALetras(today.monthValue)
        val yLiteral = today.year.toString()
        val yLetras = LegalFormatters.numeroALetras(today.year)

        val exp = LegalFormatters.toUpperCaseKeepAccents(data.expedienteNro)
        val tipoAud = LegalFormatters.toUpperCaseKeepAccents(data.tipoAudienciaReprogramada)
        val motivo = LegalFormatters.toUpperCaseKeepAccents(data.motivoReprogramacion)
        val sec = LegalFormatters.toUpperCaseKeepAccents(data.nombreSecretario)
        val juez = LegalFormatters.toUpperCaseKeepAccents(data.nombreJuez)
        val imp = LegalFormatters.toUpperCaseKeepAccents(data.nombreImputado)
        val del = LegalFormatters.toUpperCaseKeepAccents(data.delito)
        val per = LegalFormatters.toUpperCaseKeepAccents(data.perjudicado)
        var nuevaFechaLetras = data.nuevaFecha
        try {
            val parts = data.nuevaFecha.split("-", "/")
            if (parts.size == 3) {
                val yy = if (parts[0].length == 4) parts[0].toInt() else parts[2].toInt()
                val mm = parts[1].toInt()
                val dd = if (parts[0].length == 4) parts[2].toInt() else parts[0].toInt()
                val yyyyLetras = LegalFormatters.numeroALetras(yy).uppercase()
                val ddLetras = LegalFormatters.numeroALetras(dd).uppercase()
                
                // Get day of week
                val dateObj = java.time.LocalDate.of(yy, mm, dd)
                val dayOfWeek = dateObj.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("es", "ES")).uppercase()
                
                nuevaFechaLetras = "${dayOfWeek} ${ddLetras} ($dd) DE ${LegalFormatters.mesALetras(mm).uppercase()} DEL AÑO ${yyyyLetras}"
            }
        } catch (e: Exception) {}
        val nuevaFecha = LegalFormatters.toUpperCaseKeepAccents(nuevaFechaLetras)

        val nuevaHora = LegalFormatters.horaALetrasYNumeros(data.nuevaHora)
        val art = LegalFormatters.toUpperCaseKeepAccents(data.articuloCpp)
        
        val htmlBuilder = java.lang.StringBuilder()
        htmlBuilder.append("<div style=\"font-family: serif; font-size: 14pt; text-align: justify; line-height: 1.5; padding: 20px;\">")
        
        if (isConstancia) {
            htmlBuilder.append("<p style=\"text-align: right;\"><strong>EXP. <b>$exp</b></strong></p>")
            htmlBuilder.append("<p style=\"text-align: center;\"><strong>CONSTANCIA</strong></p>")
            htmlBuilder.append("<p>El infrascrito Secretario(a) Adjunto(a) del Juzgado de Letras Penal de San Pedro Sula, departamento de Cortés, HACE CONSTAR: Que se reprograma la audiencia <b>$tipoAud</b> en virtud que <b>$motivo</b>. - firmando para constancia el/la secretario(a) adjunto(a) que da fe San Pedro Sula, $dLiteral DE $mLiteral DE $yLiteral.</p>")
            htmlBuilder.append("<br><p style=\"text-align: center;\"><strong>ABOG. <b>$sec</b><br>SECRETARIO(A) ADJUNTO(A)</strong></p><br><br>")
        }
        
        htmlBuilder.append("<p style=\"text-align: justify;\"><strong>JUZGADO DE LETRAS PENAL DE LA SECCIÓN JUDICIAL DE SAN PEDRO SULA, CORTÉS, A LOS $dLetras DIAS DEL MES DE $mLiteral DEL AÑO $yLetras. –</strong></p>")
        
        htmlBuilder.append("<p>Vista la constancia que antecede rendida por la secretaria del Despacho, al efecto este Juzgado Resuelve: Reprogramar de oficio la audiencia <b>$tipoAud</b>, en la causa instruida contra <b>$imp</b>, a quien se le supone responsable del delito de <b>$del</b>, en perjuicio de <b>$per</b>, para el día <b>$nuevaFecha</b> a las <b>$nuevaHora</b>, a la cual deberán comparecer las partes intervinientes en el proceso ordenándosele al receptor del despacho que proceda a notificar en legal y debida forma para que comparezcan el día y hora señalado.- Artículo <b>$art</b> del Código Procesal Penal. – NOTIFÍQUESE.</p>")
        
        htmlBuilder.append("<br><br>")
        htmlBuilder.append("<div style=\"text-align: center;\">")
        htmlBuilder.append("<p><strong>ABOG. <b>$juez</b> - JUEZ<br>JUZGADO DE LETRAS PENAL</strong></p>")
        htmlBuilder.append("<br><br><br>")
        htmlBuilder.append("<p><strong>ABOG. <b>$sec</b><br>SECRETARIO(A) ADJUNTO(A)</strong></p>")
        htmlBuilder.append("</div>")
        
        htmlBuilder.append("</div>")
        return htmlBuilder.toString()
    }
}
