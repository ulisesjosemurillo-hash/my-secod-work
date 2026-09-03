with open('/app/applet/app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Just replace the literal match
target = """text = "Todavía no se ha generado el acta.
Llene los datos y presione Generar Acta.","""
replacement = """text = "Todavía no se ha generado el acta.\\nLlene los datos y presione Generar Acta.","""

content = content.replace(target, replacement)

with open('/app/applet/app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
