import re

with open('/app/applet/app/src/main/java/com/example/RichTextEditor.kt', 'r') as f:
    rte_content = f.read()

rte_content = rte_content.replace('import android.net.Uri\nEOF', '')
rte_content = rte_content.replace('import android.net.Uri\n', '')
rte_content = 'import android.net.Uri\n' + rte_content

with open('/app/applet/app/src/main/java/com/example/RichTextEditor.kt', 'w') as f:
    f.write(rte_content)

with open('/app/applet/app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    main_content = f.read()

if 'import androidx.compose.foundation.horizontalScroll' not in main_content:
    main_content = main_content.replace('import androidx.compose.foundation.verticalScroll', 'import androidx.compose.foundation.verticalScroll\nimport androidx.compose.foundation.horizontalScroll')

with open('/app/applet/app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(main_content)
