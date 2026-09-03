import re

with open('/app/applet/app/src/main/java/com/example/GeminiService.kt', 'r') as f:
    content = f.read()

target = """        val prompt = \"\"\"
            Analiza esta imagen de un acta de reprogramación de audiencia de Honduras o expediente judicial.
            REGLAS ESTRICTAS:
            1. BAJO NINGUNA CIRCUNSTANCIA inventes datos, nombres, fechas, delitos, ni artículos.
            2. Si no puedes leer algo por la calidad de la imagen, o si el dato no está presente, devuelve una cadena vacía "".
            3. Devuelve los resultados en formato JSON estricto, sin bloques de código Markdown, usando las siguientes claves exactas:
            {
                "expedienteNro": "...",
                "nombreImputado": "...",
                "delito": "...",
                "perjudicado": "...",
                "tipoAudienciaReprogramada": "...",
                "nuevaFecha": "YYYY-MM-DD",
                "nuevaHora": "HH:MM",
                "nombreJuez": "...",
                "nombreSecretario": "...",
                "articuloCpp": "..."
            }
        \"\"\".trimIndent()"""

replacement = """        val prompt = \"\"\"
            Eres un experto legal de Honduras analizando un acta judicial, requerimiento fiscal o expediente.
            Tu tarea es extraer meticulosamente la información del documento en la imagen para autocompletar un formulario.
            
            EXTRAE ESTOS CAMPOS DE FORMA INTELIGENTE:
            - "expedienteNro": Número de expediente (ej. 0801-2023-12345). Asegúrate de incluir el año si está presente.
            - "nombreImputado": Nombres completos del acusado, imputado o procesado.
            - "delito": Delito(s) por los cuales se le acusa (ej. ROBO AGRAVADO, ASESINATO).
            - "perjudicado": Nombre de la víctima o perjudicado (a veces dice "en perjuicio de...").
            - "tipoAudienciaReprogramada": Tipo de audiencia mencionada (ej. Audiencia Inicial, Preliminar, Juicio Oral y Público).
            - "nuevaFecha": Cualquier fecha de reprogramación o fecha de la audiencia en formato DD/MM/YYYY.
            - "nuevaHora": La hora mencionada para la audiencia en formato HH:MM (24 hrs).
            - "nombreJuez": Juez que firma o preside.
            - "nombreSecretario": Secretario(a) que autoriza o da fe.
            - "articuloCpp": Artículos del Código Procesal Penal (CPP) o Código Penal mencionados.

            INSTRUCCIONES IMPORTANTES:
            1. Analiza el contexto. A veces los nombres están después de "instruido contra" o "en perjuicio de".
            2. Extrae lo más que puedas. Si un campo no está, déjalo como "". No inventes.
            3. RESPONDE ÚNICAMENTE CON EL JSON PURO, sin comillas invertidas ni bloques (sin ```json).
            
            JSON ESPERADO:
            {
                "expedienteNro": "",
                "nombreImputado": "",
                "delito": "",
                "perjudicado": "",
                "tipoAudienciaReprogramada": "",
                "nuevaFecha": "",
                "nuevaHora": "",
                "nombreJuez": "",
                "nombreSecretario": "",
                "articuloCpp": ""
            }
        \"\"\".trimIndent()"""

content = content.replace(target, replacement)

with open('/app/applet/app/src/main/java/com/example/GeminiService.kt', 'w') as f:
    f.write(content)
