![termux-tailscale](/temp/termux-tailscale.png)

![GitHub stars](https://img.shields.io/github/stars/banyadm/termux-tailscale?style=for-the-badge) 
![GitHub License](https://img.shields.io/badge/license-MIT-green?style=for-the-badge)

---

### -- NOTICE ⚠️ --

>As of Google’s new September update, changes to the APK installation and building process may affect how this repository works.

>The repository may no longer work as intended due to the new signing requirements. The signing process requires a private key that can only be obtained after verifying your government ID and paying a $25 fee.

>So, enjoy this tool while it lasts. If the update is fully rolled out, there is a high chance that this repository will either be archived or stop working.

Thank you all for using this tool and supporting the project. ❤️

# termux-tailscale

A lightweight Termux CLI wrapper for controlling a patched Tailscale Android build via broadcast intents.

This project bridges Termux with a modified Tailscale Android app using a plugin receiver, allowing VPN control and basic status queries directly from the shell.

---

## Installation
you gotta have `curl` installed before you run the installation!

1. Install the `termux-tailscale` package
```bash
bash <(curl -Lf https://raw.githubusercontent.com/banyadm/termux-tailscale/main/install.sh)
```
2. Install the `termux-tailscale` apk plugin, make sure the actual tailscale app and plugin dont interfere!
- Install the apk [Here](https://github.com/banyadm/termux-tailscale/releases/latest)

---

## Features

- Connect / Disconnect Tailscale VPN
- Toggle VPN state
- Show connection status (tun interface detection)
- Set exit node
- Get Tailscale IP (via tun interface)
- Tailscale web client
- so much more!

---

## Requirements

- `ifconfig` available in Termux (`pkg install net-tools`)
- `curl` available in Termux (`pkg install curl`)
- `python3` available in Termux (`pkg install python`)

---

## Security

The Termux-tailscale plugin is a modified version of the [tailscale](https://github.com/tailscale/tailscale-android) Client

it adds:

- `TermuxPluginReceiver` Broadcast Receiver
- `TermuxBootReceiver` Broadcast Receiver

These Broadcasts are made to receive broadcasts from termux to start/stop the Tailscale client

- A Different app info tab
- New icon, pkg name, app name

for more debugging, There is an open-sourced [Repository](https://github.com/banyadm/tailscale-android)


## LICENSE
`MIT License
Copyright (c) 2026 Bany Adm`

[HERE](https://raw.githubusercontent.com/banyadm/termux-tailscale/main/LICENSE)
