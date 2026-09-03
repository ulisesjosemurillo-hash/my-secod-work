import re

with open('/app/applet/app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

target = """                Button(
                    onClick = { requestPermissionLauncher.launch(Manifest.permission.CAMERA) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Cámara")
                    Spacer(Modifier.width(8.dp))
                    Text("Digitalizar Acta")
                }"""

replacement = """                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { requestPermissionLauncher.launch(Manifest.permission.CAMERA) },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Cámara")
                        Spacer(Modifier.width(4.dp))
                        Text("Cámara")
                    }
                    Button(
                        onClick = { pickMedia.launch(androidx.activity.result.PickVisualMediaRequest(androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading
                    ) {
                        Icon(Icons.Default.Image, contentDescription = "Galería")
                        Spacer(Modifier.width(4.dp))
                        Text("Galería")
                    }
                }"""

content = content.replace(target, replacement)

with open('/app/applet/app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
