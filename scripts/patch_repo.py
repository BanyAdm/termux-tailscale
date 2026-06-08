import os
import re
import shutil

MANIFEST = 'android/src/main/AndroidManifest.xml'
BUILD_GRADLE = 'android/build.gradle'
SCRIPTS_DIR = os.path.dirname(os.path.abspath(__file__))

# IMPORTANT: We only change applicationId, NOT namespace or Java package names.
# The Go JNI bridge uses com.tailscale.ipn internally and must not be renamed.
OLD_APP_ID = 'com.tailscale.ipn'
NEW_APP_ID = 'com.banyadm.tmx.tailscale'
JAVA_DIR = 'android/src/main/java/com/tailscale/ipn'

# ── 1. Copy plugin Java files (keep com.tailscale.ipn package) ───────────────
for fname in ['TermuxPluginReceiver.java', 'TermuxBootReceiver.java',
              'ToggleVPNWorker.java', 'StatusVPNWorker.java']:
    src = os.path.join(SCRIPTS_DIR, fname)
    dst = os.path.join(JAVA_DIR, fname)
    if os.path.exists(src):
        shutil.copy2(src, dst)
        print(f"Copied {fname}")

# Fix package declaration in plugin files to use com.tailscale.ipn
for fname in ['TermuxPluginReceiver.java', 'TermuxBootReceiver.java',
              'ToggleVPNWorker.java', 'StatusVPNWorker.java']:
    path = os.path.join(JAVA_DIR, fname)
    if os.path.exists(path):
        with open(path) as f:
            c = f.read()
        c = c.replace('package com.banyadm.tmx.tailscale;', 'package com.tailscale.ipn;')
        with open(path, 'w') as f:
            f.write(c)

# ── 2. Change ONLY applicationId in build.gradle ─────────────────────────────
with open(BUILD_GRADLE) as f:
    gradle = f.read()
gradle = gradle.replace(
    f"applicationId '{OLD_APP_ID}'",
    f"applicationId '{NEW_APP_ID}'"
).replace(
    f'applicationId "{OLD_APP_ID}"',
    f'applicationId "{NEW_APP_ID}"'
)
with open(BUILD_GRADLE, 'w') as f:
    f.write(gradle)
print("build.gradle applicationId patched")

# ── 3. Patch manifest ─────────────────────────────────────────────────────────
with open(MANIFEST) as f:
    c = f.read()

# Change app label only
c = c.replace('android:label="Tailscale"', 'android:label="Termux:Tailscale"')

# Remove old Termux receivers if present
c = re.sub(r'\s*<receiver android:name="[^"]*TermuxPluginReceiver".*?</receiver>\s*', '\n', c, flags=re.DOTALL)
c = re.sub(r'\s*<receiver android:name="[^"]*TermuxBootReceiver".*?</receiver>\s*', '\n', c, flags=re.DOTALL)

# Use com.tailscale.ipn for receiver actions since that's still the Java package
receivers = """
        <receiver android:name=".TermuxPluginReceiver" android:exported="true">
            <intent-filter>
                <action android:name="com.banyadm.tmx.tailscale.CONNECT_VPN" />
                <action android:name="com.banyadm.tmx.tailscale.DISCONNECT_VPN" />
                <action android:name="com.banyadm.tmx.tailscale.USE_EXIT_NODE" />
                <action android:name="com.banyadm.tmx.tailscale.TERMUX_TOGGLE" />
                <action android:name="com.banyadm.tmx.tailscale.TERMUX_STATUS" />
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

# ── 4. Change app_name string ────────────────────────────────────────────────
strings_path = 'android/src/main/res/values/strings.xml'
if os.path.exists(strings_path):
    with open(strings_path) as f:
        s = f.read()
    s = s.replace('>Tailscale<', '>Termux:Tailscale<')
    s = s.replace('"Tailscale"', '"Termux:Tailscale"')
    with open(strings_path, 'w') as f:
        f.write(s)
    print("strings.xml patched")

# ── 5. Update shell script PKG ───────────────────────────────────────────────
script_path = os.path.join(SCRIPTS_DIR, 'tailscale')
if os.path.exists(script_path):
    with open(script_path) as f:
        sh = f.read()
    sh = sh.replace('PKG="com.tailscale.ipn"', f'PKG="{NEW_APP_ID}"')
    with open(script_path, 'w') as f:
        f.write(sh)
    print("tailscale shell script PKG updated")

print("\nAll done!")
