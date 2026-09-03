import os
import re

# 1. ReprogramacionData
f_rep = 'app/src/main/java/com/example/ReprogramacionData.kt'
with open(f_rep, 'r') as f:
    rep = f.read()
rep = re.sub(r'\s*val fechaOriginal: String = "",\n\s*val horaOriginal: String = "",', '', rep)
with open(f_rep, 'w') as f:
    f.write(rep)

# 2. MainViewModel
f_vm = 'app/src/main/java/com/example/MainViewModel.kt'
with open(f_vm, 'r') as f:
    vm = f.read()
# Removing fechaOriginal / horaOriginal references in recovered
vm = re.sub(r'\s*fechaOriginal\s*=\s*prefs\.getString\("fechaOriginal", ""\)\s*\?:\s*"",', '', vm)
vm = re.sub(r'\s*horaOriginal\s*=\s*prefs\.getString\("horaOriginal", ""\)\s*\?:\s*"",', '', vm)
# Removing from prefs.edit()
vm = re.sub(r'\s*\.putString\("fechaOriginal", data\.fechaOriginal\)', '', vm)
vm = re.sub(r'\s*\.putString\("horaOriginal", data\.horaOriginal\)', '', vm)
# In applyExtractedData
vm = re.sub(r'\s*found\["Fecha Original"\] = data\.fechaOriginal != "NO IDENTIFICADO" && data\.fechaOriginal\.isNotBlank\(\)', '', vm)
vm = re.sub(r'\s*found\["Hora Original"\] = data\.horaOriginal != "NO IDENTIFICADO" && data\.horaOriginal\.isNotBlank\(\)', '', vm)
with open(f_vm, 'w') as f:
    f.write(vm)

