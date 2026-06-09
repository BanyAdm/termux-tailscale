# Tailscale Termux Controller

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
- Simple broadcast-based IPC with Android app

---

## Requirements

- Termux
- Android build of Tailscale with plugin receiver support
- `ifconfig` available in Termux (`pkg install net-tools`)
- Proper package alignment between:
  - Android app package (`PKG`)
    - Termux receiver component

    ---

    ## Installation

    1. Clone or copy script into Termux:

    ```bash
    chmod +x tailscale
