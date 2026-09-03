import re

with open('/app/applet/app/src/main/AndroidManifest.xml', 'r') as f:
    content = f.read()

target = '<uses-permission android:name="android.permission.INTERNET" />'
replacement = '<uses-permission android:name="android.permission.INTERNET" />\n    <uses-permission android:name="android.permission.RECORD_AUDIO" />'

content = content.replace(target, replacement)

with open('/app/applet/app/src/main/AndroidManifest.xml', 'w') as f:
    f.write(content)
