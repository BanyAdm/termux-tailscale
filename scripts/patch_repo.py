import os
import shutil
import subprocess

JAVA_DIR = 'android/src/main/java/com/tailscale/ipn'
MANIFEST = 'android/src/main/AndroidManifest.xml'
SCRIPTS_DIR = os.path.dirname(os.path.abspath(__file__))

# 1. Copy plugin Java files into the fork
for fname in ['TermuxPluginReceiver.java', 'TermuxBootReceiver.java',
              'ToggleVPNWorker.java', 'StatusVPNWorker.java']:
    src = os.path.join(SCRIPTS_DIR, fname)
    dst = os.path.join(JAVA_DIR, fname)
    if os.path.exists(src):
        shutil.copy2(src, dst)
        print(f"Copied {fname}")
    else:
        print(f"WARNING: {fname} not found in scripts dir")

# 2. Patch manifest - add receivers if not already present
with open(MANIFEST) as f:
    c = f.read()

receivers = """
        <receiver android:name=".TermuxPluginReceiver" android:exported="true">
            <intent-filter>
                <action android:name="com.tailscale.ipn.CONNECT_VPN" />
                <action android:name="com.tailscale.ipn.DISCONNECT_VPN" />
                <action android:name="com.tailscale.ipn.USE_EXIT_NODE" />
                <action android:name="com.tailscale.ipn.TERMUX_TOGGLE" />
                <action android:name="com.tailscale.ipn.TERMUX_STATUS" />
            </intent-filter>
        </receiver>

        <receiver android:name=".TermuxBootReceiver" android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.BOOT_COMPLETED" />
                <action android:name="android.intent.action.MY_PACKAGE_REPLACED" />
            </intent-filter>
        </receiver>
"""

# Remove old incomplete receiver registrations first
import re
c = re.sub(r'\s*<receiver android:name="\.TermuxPluginReceiver".*?</receiver>\s*', '\n', c, flags=re.DOTALL)
c = re.sub(r'\s*<receiver android:name="\.TermuxBootReceiver".*?</receiver>\s*', '\n', c, flags=re.DOTALL)

# Also remove the old IPNReceiver block since TermuxPluginReceiver now handles those intents too
# (keep IPNReceiver for backwards compat with external apps)

# Add fresh receivers before </application>
c = c.replace('</application>', receivers + '\n    </application>')

with open(MANIFEST, 'w') as f:
    f.write(c)

print("Manifest patched OK")
print("Done!")
