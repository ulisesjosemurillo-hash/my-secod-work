package com.example

import android.graphics.Bitmap
import android.util.Base64
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import java.io.ByteArrayOutputStream

@Composable
fun RichTextEditor(
    initialHtml: String,
    onHtmlChanged: (String) -> Unit,
    onInsertImageClick: () -> Unit,
    imageToInsert: Bitmap?,
    onImageInserted: () -> Unit,
    modifier: Modifier = Modifier
) {
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    LaunchedEffect(imageToInsert) {
        imageToInsert?.let { bmp ->
            val baos = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.JPEG, 70, baos)
            val b64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
            val dataUrl = "data:image/jpeg;base64,$b64"
            webViewRef?.evaluateJavascript("insertImage('$dataUrl');", null)
            onImageInserted()
        }
    }
    
    // Handle HTML sync initially
    LaunchedEffect(initialHtml) {
        if (webViewRef != null && initialHtml.isNotBlank()) {
            val b64 = Base64.encodeToString(initialHtml.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
            webViewRef?.evaluateJavascript(
                "var newHtml = decodeURIComponent(escape(window.atob('$b64'))); if (getHtml() !== newHtml) { setHtml(newHtml); }"
            ) {}
        }
    }

    Column(modifier = modifier) {
        // Toolbar
        ScrollableToolbar { cmd, arg ->
            if (cmd == "changeCase") {
                webViewRef?.evaluateJavascript("changeCase('$arg');", null)
            } else {
                webViewRef?.evaluateJavascript("execCmd('$cmd', '$arg');", null)
            }
        }
        
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                ,
            factory = { context ->
                WebView(context).apply {
                    layoutParams = android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    setLayerType(android.view.View.LAYER_TYPE_SOFTWARE, null)
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.setSupportZoom(true)
                    settings.builtInZoomControls = true
                    settings.displayZoomControls = false
                    webViewClient = WebViewClient()
                    webChromeClient = WebChromeClient()
                    
                    addJavascriptInterface(object : Any() {
                        @JavascriptInterface
                        fun onContentChanged(html: String) {
                            onHtmlChanged(html)
                        }
                    }, "Android")
                    
                    val htmlContent = """
                        <!DOCTYPE html>
                        <html>
                        <head>
                            <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                            <style>
                                body { 
                                    font-family: Arial, sans-serif; 
                                    padding: 16px; 
                                    margin: 0; 
                                    outline: none;
                                    line-height: 1.5;
                                    font-size: 14px;
                                    color: #000;
                                }
                                img { max-width: 100%; height: auto; }
                                p { margin: 8px 0; }
                                .document-header { font-weight: bold; text-align: justify; }
                                .document-title { text-align: center; font-weight: bold; text-decoration: underline; }
                                b, strong { font-weight: bold; }
                                i, em { font-style: italic; }
                                u { text-decoration: underline; }
                            </style>
                        </head>
                        <body contenteditable="true" id="editor">
                        </body>
                        <script>
                            const editor = document.getElementById('editor');
                            
                            
                            function reportChange() {
                                Android.onContentChanged(editor.innerHTML);
                                
                            }
                            
                            editor.addEventListener('input', reportChange);
                            editor.addEventListener('keyup', reportChange);
                            editor.addEventListener('paste', reportChange);
                            
                            function setHtml(html) { 
                                editor.innerHTML = html; 
                            }
                            function getHtml() { 
                                return editor.innerHTML; 
                            }
                            function execCmd(command, value) { 
                                document.execCommand(command, false, value); 
                                editor.focus();
                                reportChange();
                            }
                            function insertImage(dataUrl) {
                                document.execCommand('insertImage', false, dataUrl);
                                reportChange();
                            }
                            function changeCase(type) {
                                let sel = window.getSelection();
                                if(sel.rangeCount > 0 && !sel.isCollapsed) {
                                    let range = sel.getRangeAt(0);
                                    let text = range.toString();
                                    if(type === 'upper') text = text.toUpperCase();
                                    else if(type === 'lower') text = text.toLowerCase();
                                    else if(type === 'title') text = text.replace(/\w\S*/g, function(txt){return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();});
                                    document.execCommand('insertText', false, text);
                                    reportChange();
                                }
                            }
                        </script>
                        </html>
                    """.trimIndent()
                    
                    loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
                    webViewRef = this
                }
            }
        )
    }
}

@Composable
fun ScrollableToolbar(onCommand: (String, String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Typography
        IconButton(onClick = { onCommand("bold", "") }) { Icon(Icons.Default.FormatBold, "Negrita") }
        IconButton(onClick = { onCommand("italic", "") }) { Icon(Icons.Default.FormatItalic, "Cursiva") }
        IconButton(onClick = { onCommand("underline", "") }) { Icon(Icons.Default.FormatUnderlined, "Subrayado") }
        
        // Uppercase
        IconButton(onClick = { onCommand("changeCase", "upper") }) { 
            Text("AA", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        }
        IconButton(onClick = { onCommand("removeFormat", "") }) { 
            Icon(Icons.Default.FormatClear, "Borrar Formato")
        }
        
        Spacer(Modifier.width(8.dp))
        
        // Alignment
        IconButton(onClick = { onCommand("justifyLeft", "") }) { Icon(Icons.Default.FormatAlignLeft, "Izquierda") }
        IconButton(onClick = { onCommand("justifyCenter", "") }) { Icon(Icons.Default.FormatAlignCenter, "Centro") }
        IconButton(onClick = { onCommand("justifyRight", "") }) { Icon(Icons.Default.FormatAlignRight, "Derecha") }
        IconButton(onClick = { onCommand("justifyFull", "") }) { Icon(Icons.Default.FormatAlignJustify, "Justificar") }
        
        Spacer(Modifier.width(8.dp))
        
        // History
        IconButton(onClick = { onCommand("undo", "") }) { Icon(Icons.Default.Undo, "Deshacer") }
        IconButton(onClick = { onCommand("redo", "") }) { Icon(Icons.Default.Redo, "Rehacer") }
        
        Spacer(Modifier.width(8.dp))
        
        // Lists & Indent
        IconButton(onClick = { onCommand("insertUnorderedList", "") }) { Icon(Icons.Default.FormatListBulleted, "Viñetas") }
        IconButton(onClick = { onCommand("outdent", "") }) { Icon(Icons.Default.FormatIndentDecrease, "Reducir Sangría") }
        IconButton(onClick = { onCommand("indent", "") }) { Icon(Icons.Default.FormatIndentIncrease, "Aumentar Sangría") }
    }
}
