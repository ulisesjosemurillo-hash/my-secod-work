import re

with open('/app/applet/app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'requestPermissionLauncher: androidx.activity.result.ActivityResultLauncher<String>,',
    'requestPermissionLauncher: androidx.activity.result.ActivityResultLauncher<String>,\n    pickMedia: androidx.activity.result.ActivityResultLauncher<androidx.activity.result.PickVisualMediaRequest>,'
)

content = content.replace(
    'requestPermissionLauncher = requestPermissionLauncher, onOpenCalendar = { showCalendarDialog = true },',
    'requestPermissionLauncher = requestPermissionLauncher, pickMedia = pickMedia, onOpenCalendar = { showCalendarDialog = true },'
)

with open('/app/applet/app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
