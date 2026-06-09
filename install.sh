set -e
echo "Installing termux-tailscale.."
curl -L -o $PREFIX/bin/tailscale "https://raw.githubusercontent/banyadm/termux-tailscale/main/scripts/tailscale"
chmod +x $PREFIX/bin/tailscale
echo "termux-tailscale is done installing! to use it run 'tailscale'"
