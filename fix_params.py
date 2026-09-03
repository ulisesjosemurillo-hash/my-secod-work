import re

with open('/app/applet/app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

content = content.replace('startVoice = ::startVoice, onOpenCalendar = { showCalendarDialog = true }', 'onOpenCalendar = { showCalendarDialog = true }')
content = content.replace('requestPermissionLauncher: androidx.activity.result.ActivityResultLauncher<String>, startVoice: (String) -> Unit,', 'requestPermissionLauncher: androidx.activity.result.ActivityResultLauncher<String>,')

with open('/app/applet/app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
