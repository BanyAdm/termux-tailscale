import os
import re
import shutil

JAVA_DIR = 'android/src/main/java/com/tailscale/ipn'
MANIFEST = 'android/src/main/AndroidManifest.xml'
BUILD_GRADLE = 'android/build.gradle'
SCRIPTS_DIR = os.path.dirname(os.path.abspath(__file__))

OLD_PKG = 'com.tailscale.ipn'
NEW_PKG = 'com.banyadm.tmx.tailscale'

# ── 1. Copy plugin Java files ────────────────────────────────────────────────
for fname in ['TermuxPluginReceiver.java', 'TermuxBootReceiver.java',
              'ToggleVPNWorker.java', 'StatusVPNWorker.java']:
    src = os.path.join(SCRIPTS_DIR, fname)
    dst = os.path.join(JAVA_DIR, fname)
    if os.path.exists(src):
        shutil.copy2(src, dst)
        print(f"Copied {fname}")

# ── 2. Rename package in build.gradle ───────────────────────────────────────
with open(BUILD_GRADLE) as f:
    gradle = f.read()
gradle = gradle.replace(f"namespace '{OLD_PKG}'", f"namespace '{NEW_PKG}'")
gradle = gradle.replace(f'namespace "{OLD_PKG}"', f'namespace "{NEW_PKG}"')
gradle = gradle.replace(f"applicationId '{OLD_PKG}'", f"applicationId '{NEW_PKG}'")
gradle = gradle.replace(f'applicationId "{OLD_PKG}"', f'applicationId "{NEW_PKG}"')
with open(BUILD_GRADLE, 'w') as f:
    f.write(gradle)
print("build.gradle patched")

# ── 3. Rename package in all Kotlin/Java source files ───────────────────────
for root, dirs, files in os.walk('android/src'):
    for fname in files:
        if fname.endswith(('.kt', '.java')):
            path = os.path.join(root, fname)
            try:
                with open(path, encoding='utf-8') as f:
                    c = f.read()
                if OLD_PKG in c:
                    with open(path, 'w', encoding='utf-8') as f:
                        f.write(c.replace(OLD_PKG, NEW_PKG))
            except (UnicodeDecodeError, IOError):
                pass  # skip binary files

# ── 4. Rename package in res XML files ──────────────────────────────────────
for root, dirs, files in os.walk('android/src/main/res'):
    for fname in files:
        if fname.endswith('.xml'):
            path = os.path.join(root, fname)
            try:
                with open(path, encoding='utf-8') as f:
                    c = f.read()
                if OLD_PKG in c:
                    with open(path, 'w', encoding='utf-8') as f:
                        f.write(c.replace(OLD_PKG, NEW_PKG))
            except (UnicodeDecodeError, IOError):
                pass  # skip binary files

# ── 5. Move Java source directory ───────────────────────────────────────────
old_java = 'android/src/main/java/com/tailscale/ipn'
new_java = 'android/src/main/java/com/banyadm/tmx/tailscale'
os.makedirs(new_java, exist_ok=True)
for fname in os.listdir(old_java):
    src = os.path.join(old_java, fname)
    dst = os.path.join(new_java, fname)
    if os.path.isfile(src):
        shutil.copy2(src, dst)
# Also copy subdirs
for subdir in ['ui', 'mdm', 'util']:
    s = os.path.join(old_java, subdir)
    d = os.path.join(new_java, subdir)
    if os.path.exists(s):
        if os.path.exists(d):
            shutil.rmtree(d)
        shutil.copytree(s, d)
# Remove old directory
shutil.rmtree(old_java)
# Remove empty parent dirs
for p in ['android/src/main/java/com/tailscale',
          'android/src/main/java/com/banyadm/tmx']:
    pass  # keep com/banyadm/tmx as it's needed

print("Java sources moved to new package dir")

# ── 6. Patch manifest ───────────────────────────────────────────────────────
with open(MANIFEST) as f:
    c = f.read()

c = c.replace(OLD_PKG, NEW_PKG)

# Change app label
c = c.replace('android:label="Tailscale"', 'android:label="Termux:Tailscale"')

# Remove old Termux receivers if present (will re-add)
c = re.sub(r'\s*<receiver android:name="[^"]*TermuxPluginReceiver".*?</receiver>\s*', '\n', c, flags=re.DOTALL)
c = re.sub(r'\s*<receiver android:name="[^"]*TermuxBootReceiver".*?</receiver>\s*', '\n', c, flags=re.DOTALL)

receivers = f"""
        <receiver android:name=".TermuxPluginReceiver" android:exported="true">
            <intent-filter>
                <action android:name="{NEW_PKG}.CONNECT_VPN" />
                <action android:name="{NEW_PKG}.DISCONNECT_VPN" />
                <action android:name="{NEW_PKG}.USE_EXIT_NODE" />
                <action android:name="{NEW_PKG}.TERMUX_TOGGLE" />
                <action android:name="{NEW_PKG}.TERMUX_STATUS" />
            </intent-filter>
        </receiver>

        <receiver android:name=".TermuxBootReceiver" android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.BOOT_COMPLETED" />
                <action android:name="android.intent.action.MY_PACKAGE_REPLACED" />
            </intent-filter>
        </receiver>
"""
c = c.replace('</application>', receivers + '\n    </application>')

with open(MANIFEST, 'w') as f:
    f.write(c)
print("Manifest patched")

# ── 7. Change app_name and about strings ─────────────────────────────────────
strings_path = 'android/src/main/res/values/strings.xml'
if os.path.exists(strings_path):
    with open(strings_path) as f:
        s = f.read()
    s = s.replace('>Tailscale<', '>Termux:Tailscale<')
    s = s.replace('"Tailscale"', '"Termux:Tailscale"')
    with open(strings_path, 'w') as f:
        f.write(s)
    print("strings.xml patched")

# ── 8. Patch AboutView.kt to show plugin info ────────────────────────────────
about_path = f'android/src/main/java/com/banyadm/tmx/tailscale/ui/view/AboutView.kt'
if os.path.exists(about_path):
    with open(about_path) as f:
        about = f.read()

    # Replace the version text block to show our info
    plugin_about = '''
    // Termux:Tailscale plugin info injected by patch
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Termux:Tailscale", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text("APP_NAME: Termux:Tailscale")
        Text("PACKAGE_NAME: com.banyadm.tmx.tailscale")
        Text("VERSION_NAME: " + BuildConfig.VERSION_NAME)
        Text("VERSION_CODE: " + BuildConfig.VERSION_CODE)
        Spacer(modifier = Modifier.height(16.dp))
        Text("A Termux plugin that lets you control Tailscale VPN from the Termux shell. Based on the official Tailscale for Android open source client.")
        Spacer(modifier = Modifier.height(8.dp))
        Text("GitHub: github.com/banyadm/termux-tailscale")
    }
    '''

    with open(about_path, 'w') as f:
        f.write(about)
    print("AboutView.kt noted (manual edit may be needed for full UI rewrite)")

# ── 9. Update shell script receiver component ────────────────────────────────
script_path = os.path.join(SCRIPTS_DIR, 'tailscale')
if os.path.exists(script_path):
    with open(script_path) as f:
        sh = f.read()
    sh = sh.replace('PKG="com.tailscale.ipn"', f'PKG="{NEW_PKG}"')
    with open(script_path, 'w') as f:
        f.write(sh)
    print("tailscale shell script PKG updated")

print("\nAll done!")
