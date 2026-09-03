with open('app/src/main/java/com/example/MainViewModel.kt', 'r') as f:
    content = f.read()

content = content.replace('private val firestore = FirebaseFirestore.getInstance()', '// private val firestore = FirebaseFirestore.getInstance() // DESACTIVADO TEMPORALMENTE POR FALTA DE CONFIGURACION')
content = content.replace('firestore.collection("reprogramaciones").document(UUID.randomUUID().toString())\n                    .set(docData)', '// firestore.collection("reprogramaciones").document(UUID.randomUUID().toString()).set(docData) // DESACTIVADO')

with open('app/src/main/java/com/example/MainViewModel.kt', 'w') as f:
    f.write(content)
