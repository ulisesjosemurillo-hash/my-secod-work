import re

with open('/app/applet/app/src/main/java/com/example/ReprogramacionData.kt', 'r') as f:
    content = f.read()

if "fechaOriginal" not in content:
    content = content.replace(
        'val nuevaFecha: String = "",',
        'val fechaOriginal: String = "",\n    val horaOriginal: String = "",\n    val nuevaFecha: String = "",'
    )
    with open('/app/applet/app/src/main/java/com/example/ReprogramacionData.kt', 'w') as f:
        f.write(content)

with open('/app/applet/app/src/main/java/com/example/MainViewModel.kt', 'r') as f:
    content = f.read()

if "fechaOriginal" not in content:
    content = content.replace(
        'nuevaFecha = if (data.nuevaFecha.isNotBlank()) data.nuevaFecha else current.nuevaFecha,',
        'fechaOriginal = current.fechaOriginal,\n                    horaOriginal = current.horaOriginal,\n                    nuevaFecha = if (data.nuevaFecha.isNotBlank()) data.nuevaFecha else current.nuevaFecha,'
    )
    with open('/app/applet/app/src/main/java/com/example/MainViewModel.kt', 'w') as f:
        f.write(content)

