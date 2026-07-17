#!/usr/bin/env zsh

set -euo pipefail

project_root="${0:A:h:h:h}"
tool_environment="$project_root/.tools/imagegen-venv"
python="$tool_environment/bin/python"
requirements="${0:A:h}/requirements.txt"
helper="${CODEX_HOME:-$HOME/.codex}/skills/.system/imagegen/scripts/remove_chroma_key.py"

if (( $# == 0 )); then
    print -u2 "Usage: $0 --input <key-background.png> --out <transparent.png> [options]"
    print -u2 ""
    print -u2 "Example:"
    print -u2 "  $0 --input tmp/imagegen/product-key.png --out assets/product-images/original/product.png --auto-key border --soft-matte --transparent-threshold 12 --opaque-threshold 220 --despill"
    exit 2
fi

if ! command -v python3 >/dev/null; then
    print -u2 "Python 3 is required to remove a chroma-key background."
    exit 1
fi

if [[ ! -x "$python" ]]; then
    python3 -m venv "$tool_environment"
fi

if ! "$python" -c 'import PIL' >/dev/null 2>&1; then
    "$python" -m pip install --requirement "$requirements"
fi

if [[ ! -f "$helper" ]]; then
    print -u2 "The Codex chroma-key helper was not found: $helper"
    exit 1
fi

exec "$python" "$helper" "$@"
