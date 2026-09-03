import re

with open('/app/applet/app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# 1. Remove voiceLauncher and startVoice
content = re.sub(r'val voiceLauncher = rememberLauncherForActivityResult.*?\}\s*\}', '', content, flags=re.DOTALL)
content = re.sub(r'fun startVoice\(.*?\)\s*\{.*?\n    \}', '', content, flags=re.DOTALL)
content = re.sub(r'var activeVoiceField by remember \{ mutableStateOf\(""\) \}', '', content)

# 2. Add PickVisualMedia for gallery
target_takePhoto = "val takePhotoHighRes = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->"
replacement_takePhoto = """
    val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
            val swBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
            capturedImageBitmap = swBitmap
            viewModel.analyzeImage(swBitmap)
        }
    }

    val takePhotoHighRes = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->"""
content = content.replace(target_takePhoto, replacement_takePhoto)

# 3. Change "Analizar Imagen" button to open a choice dialog (or just change it to two buttons or a drop-down. Two buttons is easier).
target_camera_btn = """                Button(
                    onClick = { requestPermissionLauncher.launch(android.Manifest.permission.CAMERA) },
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Cámara")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Auto-completar desde Imagen")
                }"""

replacement_camera_btn = """                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { requestPermissionLauncher.launch(android.Manifest.permission.CAMERA) },
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Cámara")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cámara")
                    }
                    Button(
                        onClick = { pickMedia.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Icon(Icons.Default.Image, contentDescription = "Galería")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Galería")
                    }
                }"""
content = content.replace(target_camera_btn, replacement_camera_btn)

# 4. Replace FieldWithMic with OutlinedTextField
target_fieldmic_def = """                @Composable
                fun FieldWithMic(label: String, value: String, onValueChange: (String) -> Unit, voiceTag: String) {
                    OutlinedTextField(
                        value = value, onValueChange = onValueChange, label = { Text(label) },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { IconButton(onClick = { startVoice(voiceTag) }) { Icon(Icons.Default.Mic, "Dictar") } }
                    )
                }"""
content = content.replace(target_fieldmic_def, "")

content = re.sub(
    r'FieldWithMic\("(.*?)", (.*?), \{ (.*?) \}, ".*?"\)',
    r'OutlinedTextField(value = \2, onValueChange = { \3 }, label = { Text("\1") }, modifier = Modifier.fillMaxWidth())',
    content
)

with open('/app/applet/app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
