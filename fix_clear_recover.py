import re

with open('/app/applet/app/src/main/java/com/example/MainViewModel.kt', 'r') as f:
    content = f.read()

target = """    fun updateData(newData: ReprogramacionData) {
        _uiState.value = newData
    }"""

replacement = """    fun updateData(newData: ReprogramacionData) {
        _uiState.value = newData
    }
    
    private var previousData: ReprogramacionData? = null
    
    fun clearData() {
        previousData = _uiState.value
        _uiState.value = ReprogramacionData()
        generateDocument()
    }
    
    fun recoverData() {
        previousData?.let {
            _uiState.value = it
            generateDocument()
        }
    }"""

content = content.replace(target, replacement)

with open('/app/applet/app/src/main/java/com/example/MainViewModel.kt', 'w') as f:
    f.write(content)
