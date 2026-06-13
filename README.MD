![termux-tailscale](/temp/termux-tailscale.png)

![GitHub stars](https://img.shields.io/github/stars/banyadm/termux-tailscale?style=for-the-badge) ![GitHub issues](https://img.shields.io/github/issues/banyadm/termux-tailscale?color=violet&style=for-the-badge) ![GitHub repo size](https://img.shields.io/github/repo-size/banyadm/termux-tailscale?style=for-the-badge) ![GitHub License](https://img.shields.io/badge/license-MIT-green?style=for-the-badge)

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

## LICENSE
`MIT License
Copyright (c) 2026 Bany Adm`

[HERE](https://raw.githubusercontent.com/banyadm/termux-tailscale/main/LICENSE)
