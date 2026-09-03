with open('app/src/main/java/com/example/MainViewModel.kt', 'r') as f:
    content = f.read()

# Replace constructor and ViewModel -> AndroidViewModel
content = content.replace(
"""class MainViewModel(
    private val localOcrService: LocalOcrService,
    private val firestore: FirebaseFirestore,
    private val prefs: SharedPreferences
) : ViewModel() {""",
"""import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val localOcrService = LocalOcrService()
    private val firestore = FirebaseFirestore.getInstance()
    private val prefs = application.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)"""
)

with open('app/src/main/java/com/example/MainViewModel.kt', 'w') as f:
    f.write(content)
