# Termux:Tailscale

A Termux plugin that embeds Tailscale as a proper Android app with:
- Shell commands (`tailscale up/down/status/toggle`)
- Homescreen widget (1×1 toggle)
- Floating overlay button
- Tasker/Locale plugin support
- Boot auto-reconnect

---

## Step 1 — One-time setup on your computer (5 min)

You need Git. Everything else runs in GitHub Actions for free.

```bash
# 1. Fork tailscale-android on GitHub (click Fork on the repo page), then:
git clone https://github.com/YOUR_USERNAME/tailscale-android
cd tailscale-android
git apply ../tailscale.patch        # applies our 20-line patch
git add -A && git commit -m "expose startVPN/stopVPN for Termux plugin"
git push
cd ..

# 2. Create your plugin repo
git clone https://github.com/YOUR_USERNAME/termux-tailscale
# copy all these project files into it, then:
cd termux-tailscale

# 3. Add tailscale-android as a submodule pointing to YOUR fork
git submodule add https://github.com/YOUR_USERNAME/tailscale-android tailscale-android
git add -A && git commit -m "initial commit"
git push
```

GitHub Actions will now automatically build the APK on every push.

---

## Step 2 — Generate a signing keystore

The APK must be signed with the **same key as your Termux installation**.

```bash
# Check which Termux source you use:
#   F-Droid  → download their debug keystore (see below)
#   GitHub   → same
#   Play     → different sharedUserId; plugin won't work

# Generate a fresh keystore (use this if building from scratch / new install):
keytool -genkey -v \
  -keystore termux-tailscale.jks \
  -alias termux_ts \
  -keyalg RSA -keysize 2048 \
  -validity 10000 \
  -storepass YOUR_STORE_PASS \
  -keypass  YOUR_KEY_PASS \
  -dname "CN=TermuxTailscale, O=Personal"

# Base64-encode it for GitHub Secrets:
base64 -w0 termux-tailscale.jks > keystore.b64
cat keystore.b64   # copy this output
```

Then in your GitHub repo → Settings → Secrets → Actions, add:

| Secret name        | Value                          |
|--------------------|-------------------------------|
| `KEYSTORE_PATH`    | `termux-tailscale.jks`        |
| `KEYSTORE_PASSWORD`| your store password           |
| `KEY_ALIAS`        | `termux_ts`                   |
| `KEY_PASSWORD`     | your key password             |

> ⚠️  **Important:** You must also sign Termux itself with this same key,
> OR extract the existing Termux signing cert and re-sign this APK with it.
> The easiest path: install Termux from GitHub releases (not Play Store),
> and use a fresh keystore for both.

---

## Step 3 — Download and install the APK

After pushing to `main`, GitHub Actions builds the APK (~3 min).

```
GitHub repo → Actions tab → latest workflow run → Artifacts → termux-tailscale-release.apk
```

In Termux, download and install:

```bash
# Option A: download via browser, then install from Files app

# Option B: if you have gh CLI installed in Termux
pkg install gh
gh auth login
gh run download --repo YOUR_USERNAME/termux-tailscale
```

Then install the APK: tap it in your file manager.

---

## Step 4 — Install the shell command in Termux

```bash
# Copy the tailscale script to your PATH
cp scripts/tailscale $PREFIX/bin/tailscale
chmod +x $PREFIX/bin/tailscale

# Done! Try it:
tailscale up
tailscale status
tailscale down
tailscale toggle
tailscale float on    # shows floating button
tailscale float off
```

---

## Step 5 — Optional: Widget & Tasker

**Widget:** Long-press homescreen → Widgets → scroll to "Termux:Tailscale" → drag the 1×1 toggle.

**Tasker:** New Task → Plugin → Termux:Tailscale → pick Connect/Disconnect/Toggle.

**Floating button:** `tailscale float on` — then grant overlay permission if prompted.

---

## How it works

```
tailscale up
  └─► am broadcast -a com.termux.tailscale.UP
        └─► TailscaleReceiver.onReceive()
              └─► VpnBridge.connect()
                    └─► TailscaleForegroundService.start()
                          └─► VpnService (Tailscale Go backend)
                                └─► WireGuard tunnel ✓
```

## Project structure

```
termux-tailscale/
├── app/src/main/
│   ├── AndroidManifest.xml          sharedUserId=com.termux, all receivers
│   └── java/com/termux/tailscale/
│       ├── VpnBridge.java           core: connect/disconnect/toggle/status
│       ├── TailscaleReceiver.java   shell broadcast handler
│       ├── TailscaleForegroundService.java  persistent notification
│       ├── VpnService.java          delegates to Tailscale Go backend
│       ├── TailscaleWidget.java     homescreen widget
│       ├── FloatService.java        draggable floating button
│       ├── TaskerReceiver.java      Tasker/Locale fire receiver
│       ├── TaskerEditActivity.java  Tasker plugin config UI
│       ├── BootReceiver.java        auto-reconnect after reboot
│       └── MainActivity.java        settings screen
├── scripts/tailscale                the shell command
├── tailscale-android/               git submodule (your fork)
├── tailscale.patch                  20-line patch to apply to fork
└── .github/workflows/build.yml      GitHub Actions build
```
