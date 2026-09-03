with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

content = content.replace('class MainActivity : ComponentActivity() {\n    override fun onCreate(savedInstanceState: Bundle?) {', 'class MainActivity : ComponentActivity() {\n    private val viewModel: MainViewModel by viewModels()\n\n    override fun onCreate(savedInstanceState: Bundle?) {')
content = content.replace('                    val viewModel: MainViewModel by viewModels()\n                    MobileAppScreen(viewModel)', '                    MobileAppScreen(viewModel)')

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
