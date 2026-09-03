import re

with open('/app/applet/app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Change RightPanel signature
content = content.replace('fun RightPanel(documentText: String, viewModel: MainViewModel, context: android.content.Context) {', 'fun RightPanel(documentText: String, viewModel: MainViewModel, context: android.content.Context, modifier: Modifier = Modifier.fillMaxWidth().fillMaxHeight()) {')

# Change RightPanel card modifier
content = content.replace('modifier = Modifier.fillMaxWidth().fillMaxHeight()\n    ) {\n        Column', 'modifier = modifier\n    ) {\n        Column')

# Update LeftPanel height issue if any - no LeftPanel doesn't have fillMaxHeight.

# Update call in MainScreen (Tablet view)
content = content.replace('RightPanel(documentText = documentText, viewModel = viewModel, context = context)\n                    }', 'RightPanel(documentText = documentText, viewModel = viewModel, context = context, modifier = Modifier.fillMaxWidth().fillMaxHeight())\n                    }')

# Update call in MainScreen (Mobile view)
content = content.replace('RightPanel(documentText = documentText, viewModel = viewModel, context = context)\n                }\n            }', 'RightPanel(documentText = documentText, viewModel = viewModel, context = context, modifier = Modifier.fillMaxWidth().height(800.dp))\n                }\n            }')

with open('/app/applet/app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)

