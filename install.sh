#!/bin/sh
# Installs Patchy from the latest GitHub release. No account needed.
#   curl -fsSL https://raw.githubusercontent.com/BlueRasputin/Patchy-patch-notes-app/main/install.sh | sh
#   ... | sh -s -- --mcp     also install the MCP server for Claude, Codex and Gemini
set -eu

BASE="${PATCHY_RELEASE_URL:-https://github.com/BlueRasputin/Patchy-patch-notes-app/releases/latest/download}"
DIR="${PATCHY_HOME:-$HOME/.patchy}"
mkdir -p "$DIR"

echo "Downloading Patchy..."
curl -fsSL "$BASE/patchy.vsix" -o "$DIR/patchy.vsix"

# One install per editor: its shell command if on PATH, else the macOS app bundle
installed=""
install_into() {
  if command -v "$1" >/dev/null 2>&1; then cli="$1"
  elif [ -n "$2" ] && [ -x "$2" ]; then cli="$2"
  else return 0
  fi
  if "$cli" --install-extension "$DIR/patchy.vsix" --force >/dev/null 2>&1; then
    echo "Installed the Patchy extension in $1"
    installed=1
  fi
}
install_into code "/Applications/Visual Studio Code.app/Contents/Resources/app/bin/code"
install_into code-insiders "/Applications/Visual Studio Code - Insiders.app/Contents/Resources/app/bin/code"
install_into cursor "/Applications/Cursor.app/Contents/Resources/app/bin/cursor"
install_into windsurf "/Applications/Windsurf.app/Contents/Resources/app/bin/windsurf"
install_into codium "/Applications/VSCodium.app/Contents/Resources/app/bin/codium"
if [ -z "$installed" ]; then
  echo "No VS Code-compatible editor found. Install $DIR/patchy.vsix with Extensions > ... > Install from VSIX."
fi

if [ "${1:-}" = "--mcp" ]; then
  if ! command -v npm >/dev/null 2>&1; then
    echo "--mcp needs Node.js and npm (https://nodejs.org)." >&2
    exit 1
  fi
  npm install -g "$BASE/patchy-mcp.tgz"
  echo "Installed the 'patchy-mcp' server. Add it to Claude Code with:"
  echo "  claude mcp add --scope user patchy -- patchy-mcp"
fi

echo "JetBrains IDEs: download $BASE/patchy-intellij.zip, then Settings > Plugins > Install Plugin from Disk."
