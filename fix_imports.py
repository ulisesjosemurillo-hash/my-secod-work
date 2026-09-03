with open('/app/applet/app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

content = content.replace('import androidx.compose.ui.draw.clip\n', '')
content = content.replace('package com.example\n', 'package com.example\n\nimport androidx.compose.ui.draw.clip\n')

with open('/app/applet/app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
