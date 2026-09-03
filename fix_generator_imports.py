with open('/app/applet/app/src/main/java/com/example/MainViewModel.kt', 'r') as f:
    content = f.read()

print("DocumentGenerator" in content)
