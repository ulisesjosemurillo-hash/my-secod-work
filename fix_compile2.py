import re

with open('/app/applet/app/src/main/java/com/example/RichTextEditor.kt', 'r') as f:
    rte_content = f.read()

rte_content = rte_content.replace('import android.net.Uri\npackage com.example', 'package com.example\nimport android.net.Uri')

with open('/app/applet/app/src/main/java/com/example/RichTextEditor.kt', 'w') as f:
    f.write(rte_content)
