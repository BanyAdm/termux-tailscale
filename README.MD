![termux-tailscale](/termux-tailscale.png)
# termux-tailscale

A lightweight Termux CLI wrapper for controlling a patched Tailscale Android build via broadcast intents.

This project bridges Termux with a modified Tailscale Android app using a plugin receiver, allowing VPN control and basic status queries directly from the shell.

---

## Features

- Connect / Disconnect Tailscale VPN
- Toggle VPN state
- Show connection status (tun interface detection)
- Set exit node
- Get Tailscale IP (via tun interface)
- Display CLI version label

---

## Requirements

- `ifconfig` available in Termux (`pkg install net-tools`)
