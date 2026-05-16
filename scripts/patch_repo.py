import sys

# 1. Patch manifest
with open('android/src/main/AndroidManifest.xml') as f:
    c = f.read()

if 'sharedUserId' not in c:
    c = c.replace(
        '<manifest xmlns:android="http://schemas.android.com/apk/res/android"',
        '<manifest xmlns:android="http://schemas.android.com/apk/res/android"\n    android:sharedUserId="com.termux"'
    )

receivers = '''
        <receiver android:name=".TermuxPluginReceiver" android:exported="true">
            <intent-filter>
                <action android:name="com.tailscale.ipn.TERMUX_UP"/>
                <action android:name="com.tailscale.ipn.TERMUX_DOWN"/>
                <action android:name="com.tailscale.ipn.TERMUX_TOGGLE"/>
                <action android:name="com.tailscale.ipn.TERMUX_STATUS"/>
            </intent-filter>
        </receiver>
        <receiver android:name=".TermuxBootReceiver" android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.BOOT_COMPLETED"/>
            </intent-filter>
        </receiver>
'''

if 'TermuxPluginReceiver' not in c:
    c = c.replace('</application>', receivers + '\n    </application>')

with open('android/src/main/AndroidManifest.xml', 'w') as f:
    f.write(c)

print("Manifest patched OK")
