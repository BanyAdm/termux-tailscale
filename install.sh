set -e

GREEN='\033[0;32m'

clear
echo "Installing Termux:Tailscale..."

WEBCLIENT_DIR="$HOME/.local/share/tailscale-web"
ASSETS_DIR="$WEBCLIENT_DIR/assets"
BASE="https://raw.githubusercontent.com/banyadm/termux-tailscale/main/webclient"

curl -L -o "$PREFIX/bin/tailscale" "https://raw.githubusercontent.com/banyadm/termux-tailscale/main/scripts/tailscale"
chmod +x "$PREFIX/bin/tailscale"

mkdir -p "$ASSETS_DIR"
curl -L -o "$WEBCLIENT_DIR/index.html" "$BASE/index.html"
curl -L -o "$WEBCLIENT_DIR/server.py" "$BASE/server.py"
curl -L -o "$ASSETS_DIR/index-BbZBz4S-.js" "$BASE/assets/index-BbZBz4S-.js"
curl -L -o "$ASSETS_DIR/index-DVk8gqX9.css" "$BASE/assets/index-DVk8gqX9.css"
curl -L -o "$ASSETS_DIR/Inter.var.latin-Dxq58mVK.woff2" "$BASE/assets/Inter.var.latin-Dxq58mVK.woff2"

mkdir -p "$HOME/.config/tailscale"
curl -s "https://raw.githubusercontent.com/banyadm/termux-tailscale/main/VERSION" > "$HOME/.config/tailscale/version"

echo -e "${GREEN}Done! Run 'tailscale' to get started."
