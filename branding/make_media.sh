#!/usr/bin/env bash
# Regenerates site/media from a real client run.
# Usage: caffeinate -dimsu ./gradlew runClientGameTest && branding/make_media.sh && python3 -P branding/make_banners.py
# (caffeinate keeps macOS from sleeping the display, which stalls the client window)
# Needs ffmpeg and ImageMagick. The chat panel text in site/index.html and the samples in
# branding/curseforge-description.md must be copied from the same run's log ("[System] [CHAT]" lines
# after "reef check").
set -euo pipefail
cd "$(dirname "$0")/.."
shots=build/run/clientGameTest/screenshots
media=site/media
font=branding/fonts/Geist.ttf
mkdir -p "$media"
frames=$(mktemp -d)
trap 'rm -rf "$frames"' EXIT

for shot in gallery_reef gallery_restored gallery_night check check_stressed bleach_before bleach_after recovered; do
	cp "$shots"/*_${shot}.png "$media/${shot//_/-}.png"
done
# The 3D logo for the site header, trimmed to its pixels.
magick branding/logo3d.png -trim +repage -resize 600x600 "$media/logo3d.png"
cp "$shots"/*_timelapse_000.png "$media/timelapse-first.png"
cp "$(find "$shots" -name '*_timelapse_*.png' | sort | tail -1)" "$media/timelapse-last.png"

# Time-lapse GIF: every frame, then a pause on the grown reef.
i=0
while IFS= read -r f; do
	cp "$f" "$frames/$(printf '%03d' "$i").png"
	i=$((i + 1))
done < <(find "$shots" -name '*_timelapse_*.png' | sort)
last=$(printf '%03d' $((i - 1)))
for _ in 1 2 3 4 5 6; do cp "$frames/$last.png" "$frames/$(printf '%03d' "$i").png"; i=$((i + 1)); done
ffmpeg -loglevel error -y -framerate 6 -i "$frames/%03d.png" \
	-vf "scale=800:-1:flags=lanczos,split[a][b];[a]palettegen=max_colors=160[p];[b][p]paletteuse=dither=bayer:bayer_scale=4" \
	-loop 0 "$media/timelapse.gif"

# Before and after, side by side, from the first and last time-lapse frames.
label() {
	magick "$1" -resize 960x -gravity northwest -font "$font" -pointsize 34 -fill white \
		-undercolor 'rgba(4,14,24,0.75)' -annotate +18+14 " $2 " "$3"
}
label "$media/timelapse-first.png" "Start: five fragments" "$frames/before.png"
label "$media/timelapse-last.png" "Later: a reef" "$frames/after.png"
magick "$frames/before.png" "$frames/after.png" +append "$media/before-after.png"
label "$media/bleach-after.png" "After a magma vent" "$frames/bleached.png"
label "$media/recovered.png" "Later: recovering" "$frames/recovering.png"
magick "$frames/bleached.png" "$frames/recovering.png" +append "$media/bleach-recover.png"

for f in "$media"/*.png; do
	magick "$f" -strip -define png:compression-level=9 "$f"
	magick "$f" -quality 82 "${f%.png}.webp"
	magick "$f" -resize 640x -quality 80 "${f%.png}-640.webp"
done
echo "media updated: $(find "$media" -name '*.png' | wc -l | tr -d ' ') images, time-lapse of $((i - 6)) frames"
