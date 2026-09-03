import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Remove height(450.dp) from MainActivity preview
content = content.replace("Box(modifier = Modifier.fillMaxWidth().height(450.dp).clip(RoundedCornerShape(8.dp)).background(Color.White))", "Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color.White))")

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)

with open('app/src/main/java/com/example/RichTextEditor.kt', 'r') as f:
    content = f.read()

# Add WRAP_CONTENT to WebView inside RichTextEditor
replacement = """WebView(context).apply {
                    layoutParams = android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                    )"""

content = content.replace("WebView(context).apply {", replacement)

# Add clear formatting button, uppercase, lowercase etc
# Update HTML template and Toolbar in RichTextEditor
html_template = """
                                body {
                                     font-family: Arial, sans-serif;
                                     padding: 16px;
                                     margin: 0;
                                     outline: none;
                                    line-height: 1.5;
                                    font-size: 14px;
                                    color: #000;
                                    min-height: 400px;
                                }"""
content = content.replace("body {\n                                     font-family: Arial, sans-serif;\n                                     padding: 16px;\n                                     margin: 0;\n                                     outline: none;\n                                    line-height: 1.5;\n                                    font-size: 14px;\n                                    color: #000;\n                                }", html_template)

# Inject resize logic to adapt wrap_content in WebView dynamically
resize_logic = """
                            function reportChange() {
                                Android.onContentChanged(editor.innerHTML);
                                Android.onHeightChanged(document.documentElement.scrollHeight);
                            }"""
content = content.replace("function reportChange() {\n                                Android.onContentChanged(editor.innerHTML);\n                            }", resize_logic)

with open('app/src/main/java/com/example/RichTextEditor.kt', 'w') as f:
    f.write(content)
