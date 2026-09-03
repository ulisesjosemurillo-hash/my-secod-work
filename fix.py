with open('app/src/main/java/com/example/MainViewModel.kt', 'r') as f:
    content = f.read()

content = content.replace('private fun recoverData()', 'fun recoverData()')

with open('app/src/main/java/com/example/MainViewModel.kt', 'w') as f:
    f.write(content)
