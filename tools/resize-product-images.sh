#!/usr/bin/env zsh

set -euo pipefail

project_root="${0:A:h:h}"
original_images="$project_root/assets/product-images/original"
resized_images="$project_root/src/main/resources/static/images/300"

if ! command -v magick >/dev/null; then
    print -u2 "ImageMagick is required. Install it with: brew install imagemagick"
    exit 1
fi

if [[ ! -d "$original_images" ]]; then
    print -u2 "Original-image directory does not exist: $original_images"
    exit 1
fi

mkdir -p "$resized_images"

found_images=false
while IFS= read -r -d '' original_image; do
    found_images=true
    filename="${original_image:t:r}.png"

    magick "$original_image" \
        -auto-orient \
        -resize '300x225>' \
        -gravity center \
        -background none \
        -extent 300x225 \
        -strip \
        "$resized_images/$filename"
done < <(find "$original_images" -maxdepth 1 -type f -iname '*.png' -print0)

if ! $found_images; then
    print -u2 "No PNG images found in: $original_images"
    exit 1
fi
