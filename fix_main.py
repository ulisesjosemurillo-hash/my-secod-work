with open('/app/applet/app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    lines = f.readlines()

new_lines = []
for i, line in enumerate(lines):
    if "fun detailRow(label: String, value: String)" in line:
        new_lines.append("                    @Composable\n")
    if "var dayEventsDate by remember { mutableStateOf<LocalDate?>(null) }" in line or "var selectedEvent by remember { mutableStateOf<CalendarEvent?>(null) }" in line:
        continue # remove from here
    if "var showAddEventDialog by remember { mutableStateOf(false) }" in line:
        new_lines.append(line)
        new_lines.append("    var dayEventsDate by remember { mutableStateOf<LocalDate?>(null) }\n")
        new_lines.append("    var selectedEvent by remember { mutableStateOf<CalendarEvent?>(null) }\n")
        continue
    new_lines.append(line)

with open('/app/applet/app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.writelines(new_lines)
