import re

with open('/app/applet/app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'capturedImageBitmap = capturedImageBitmap, requestPermissionLauncher = requestPermissionLauncher,',
    'capturedImageBitmap = capturedImageBitmap, requestPermissionLauncher = requestPermissionLauncher, pickMedia = pickMedia,'
)

with open('/app/applet/app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
