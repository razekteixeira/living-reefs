#!/usr/bin/env bash
# Checks that coral keeps its random ticks and grows with Lithium installed (Lithium replaces the
# vanilla random tick loop in sparse sections). Starts the dev server with the Lithium jar given as
# the only argument copied into run/mods, builds a sparse warm-ocean reef of 49 coral blocks on sand,
# runs it at random_tick_speed 300 and prints /reef stats. Pass: coral random ticks and growth > 0.
# Usage: tools/compat-lithium.sh path/to/lithium-fabric-<version>+mc26.3.jar   (JDK 25 as JAVA_HOME)
set -euo pipefail
cd "$(dirname "$0")/.."
jar=${1:?usage: tools/compat-lithium.sh path/to/lithium.jar}
# A fresh flat world, so only the sparse reef below is ticking.
rm -rf run/world
mkdir -p run/mods
cp "$jar" run/mods/lithium-under-test.jar
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
trap 'python3 tools/rcon.py stop > /dev/null 2>&1 || true; rm -f run/mods/lithium-under-test.jar' EXIT
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
grep -oE "lithium [0-9.+a-z]+" "$log" | head -1 || { echo "Lithium did not load, see $log" >&2; exit 1; }
rcon() { python3 tools/rcon.py "$@"; }
# A sparse reef: one coral block every 8 blocks on sand under 8 blocks of warm ocean water.
commands=("gamerule minecraft:max_block_modifications 1000000" "forceload add 296 296 359 359" "fill 299 -61 299 356 -49 356 minecraft:glass hollow"
	"fill 300 -60 300 355 -50 355 minecraft:water" "fill 300 -61 300 355 -61 355 minecraft:sand"
	"fillbiome 299 -64 299 356 -40 356 minecraft:warm_ocean")
for x in $(seq 300 8 355); do
	for z in $(seq 300 8 355); do
		commands+=("setblock $x -60 $z minecraft:tube_coral_block")
	done
done
setup=$(rcon "${commands[@]}")
if grep -qiE "too many|too large|error|unknown|incorrect|expected" <<< "$setup"; then
	echo "Setting up the reef failed:" >&2
	echo "$setup" >&2
	exit 1
fi
rcon "gamerule minecraft:random_tick_speed 300" "sleep 30" "reef stats" "tick query"
