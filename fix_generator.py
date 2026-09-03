import re

with open('app/src/main/java/com/example/DocumentGenerator.kt', 'r') as f:
    content = f.read()

# Fix nuevaFecha parsing in DocumentGenerator
# Wait, it's better to update DocumentGenerator parsing to use LegalFormatters correctly.
replacement = """
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
"""

content = re.sub(r'\s*var nuevaFechaLetras.*?val nuevaFecha = LegalFormatters\.toUpperCaseKeepAccents\(nuevaFechaLetras\)', replacement, content, flags=re.DOTALL)

# Add bold for the fields in the HTML
content = content.replace("EXP. $exp", "EXP. <b>$exp</b>")
content = content.replace("la audiencia $tipoAud", "la audiencia <b>$tipoAud</b>")
content = content.replace("virtud que $motivo.", "virtud que <b>$motivo</b>.")
content = content.replace("contra $imp", "contra <b>$imp</b>")
content = content.replace("delito de $del,", "delito de <b>$del</b>,")
content = content.replace("perjuicio de $per,", "perjuicio de <b>$per</b>,")
content = content.replace("para el día $nuevaFecha", "para el día <b>$nuevaFecha</b>")
content = content.replace("a las $nuevaHora", "a las <b>$nuevaHora</b>")
content = content.replace("Artículo $art", "Artículo <b>$art</b>")
content = content.replace("ABOG. $sec", "ABOG. <b>$sec</b>")
content = content.replace("ABOG. $juez", "ABOG. <b>$juez</b>")


with open('app/src/main/java/com/example/DocumentGenerator.kt', 'w') as f:
    f.write(content)
