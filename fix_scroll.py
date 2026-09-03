import re

with open('/app/applet/app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

replacement = """
            } else {
                val scrollState = rememberScrollState()
                val coroutineScope = rememberCoroutineScope()
                
                LaunchedEffect(documentText) {
                    if (documentText.isNotBlank()) {
                        coroutineScope.launch {
                            kotlinx.coroutines.delay(100)
                            scrollState.animateScrollTo(scrollState.maxValue)
                        }
                    }
                }

                Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(scrollState), verticalArrangement = Arrangement.spacedBy(16.dp)) {
"""

content = content.replace('            } else {\n                Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {', replacement)

with open('/app/applet/app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)

