with open('app/src/main/java/com/example/DataExtractor.kt', 'r') as f:
    content = f.read()

# I will replace standard string literals with raw string literals for Regex
# Actually, it's easier to just do text replacement on the file
import re
content = re.sub(r'Regex\("([^"]*)"\)', lambda m: 'Regex("""' + m.group(1).replace('\\', '\\\\') + '""")', content)

with open('app/src/main/java/com/example/DataExtractor.kt', 'w') as f:
    f.write(content)
