#!/usr/bin/env bash
# Measures what Living Reefs costs on the dev server: a 64 x 64 warm-ocean reef of 8,192 coral (a
# floor of coral blocks with a fan on every block, 512 coral per chunk section, so dense that nothing
# can grow and every check runs to the end). For random_tick_speed 3 (vanilla default) and 300 (a
# stress value), it waits 40 s and then takes 15 readings, 1.1 s apart, of the handler's own time in
# the last full second from /reef stats. Prints n/min/median/max in ms per second of game time.
# pause-when-empty-seconds=0 keeps the empty server ticking (it pauses after 60 s by default).
# Usage: tools/benchmark.sh   (needs JDK 25 as JAVA_HOME; uses ./run, which is gitignored)
set -euo pipefail
cd "$(dirname "$0")/.."
java_bin="${JAVA_HOME:+$JAVA_HOME/bin/}java"
if ! "$java_bin" -version 2>&1 | grep -q 'version "25'; then
	echo "JDK 25 not found: set JAVA_HOME to a JDK 25 installation" >&2
	exit 1
fi
mkdir -p run
# A fresh flat world every time, so earlier runs never change the workload.
rm -rf run/world
export RCON_PASSWORD RCON_PORT=25576
RCON_PASSWORD=$(python3 -c 'import secrets; print(secrets.token_hex(16))')
echo "eula=true" > run/eula.txt
cat > run/server.properties <<PROPS
server-ip=127.0.0.1
server-port=25566
enable-rcon=true
rcon.port=${RCON_PORT}
rcon.password=${RCON_PASSWORD}
level-type=minecraft\:flat
online-mode=false
spawn-monsters=false
pause-when-empty-seconds=0
PROPS
log=$(mktemp)
./gradlew runServer --console=plain > "$log" 2>&1 &
server=$!
trap 'python3 tools/rcon.py stop > /dev/null 2>&1 || true' EXIT
waited=0
until grep -q "RCON running" "$log"; do
	if ! kill -0 "$server" 2>/dev/null || grep -q "BUILD FAILED" "$log"; then
		echo "Dev server failed to start, see $log" >&2
		exit 1
	fi
	if [ "$waited" -ge 300 ]; then
		echo "Dev server did not start within 300 s, see $log" >&2
		exit 1
	fi
	sleep 2
	waited=$((waited + 2))
done
rcon() { python3 tools/rcon.py "$@"; }
# The flat world's grass is at y -61; the reef sits on sand at -61 with water up to -50.
# fill and fillbiome refuse more than max_block_modifications blocks (32,768 by default), so raise it
# first, and stop if any setup command reports an error instead of measuring the wrong world.
setup=$(rcon "gamerule minecraft:max_block_modifications 1000000" \
	"forceload add 96 96 167 167" \
	"fill 99 -61 99 164 -49 164 minecraft:glass hollow" \
	"fill 100 -60 100 163 -50 163 minecraft:water" \
	"fill 100 -61 100 163 -61 163 minecraft:sand" \
	"fill 100 -60 100 163 -60 163 minecraft:tube_coral_block" \
	"fill 100 -59 100 163 -59 163 minecraft:brain_coral_fan[waterlogged=true]" \
	"fillbiome 99 -64 99 164 -40 164 minecraft:warm_ocean")
if grep -qiE "too many|too large|error|unknown|incorrect|expected" <<< "$setup"; then
	echo "Setting up the reef failed:" >&2
	echo "$setup" >&2
	exit 1
fi
check=$(rcon "reef check 120 -59 120")
grep -q "warm reef water" <<< "$check" || { echo "The benchmark reef is not in reef water: $check" >&2; exit 1; }
measure() {
	local speed=$1
	rcon "gamerule minecraft:random_tick_speed ${speed}" > /dev/null
	sleep 40
	local readings=()
	for _ in $(seq 15); do readings+=("reef stats" "sleep 1.1"); done
	rcon "${readings[@]}" | grep -oE "last second [0-9.]+ ms over [0-9]+" | awk '{print $3, $6}' | sort -n \
		| awk -v s="$speed" '{a[NR]=$1; t[NR]=$2} END {printf "random_tick_speed %d: n=%d min=%.3f median=%.3f max=%.3f ms per second (coral ticks per second at median: %d)\n", s, NR, a[1], a[int((NR+1)/2)], a[NR], t[int((NR+1)/2)]}'
}
measure 3
measure 300
rcon "tick query" | head -3
# An empty dedicated server pauses after pause-when-empty-seconds; readings from a paused server are worthless.
if grep -q "Server empty for" "$log"; then
	echo "The server paused while measuring; the readings above are invalid" >&2
	exit 1
fi
