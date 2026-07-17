#!/usr/bin/env bash

set -euo pipefail

repository_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

cd "$repository_root"

imagegen_python=".tools/imagegen-venv/bin/python"

if [[ ! -x "$imagegen_python" ]]; then
  python3 -m venv .tools/imagegen-venv
fi

if ! "$imagegen_python" -c 'import PIL' >/dev/null 2>&1; then
  "$imagegen_python" -m pip install \
    --requirement tools/imagegen/requirements.txt
fi

prompt="$(cat ./assets/product-images/prompt.md)"
prompt+=$'\n\nFor chroma-key removal, invoke only ./tools/imagegen/remove-chroma-key.sh. Do not directly run python, python3, uv, pip, or another package-management command. Do not create aliases, symlinks, or modify shell configuration. Pillow is preinstalled in the project-local environment.'

/Applications/ChatGPT.app/Contents/Resources/codex exec \
  --model gpt-5.6-luna \
  --config model_reasoning_effort="low" \
  --ephemeral \
  --cd "${repository_root}" \
  --sandbox workspace-write \
  "$prompt"
