"""Copies the real /reef check output of a capture run into the site and the CurseForge description.

The capture test runs /reef check on a thriving coral and on a stressed one; the client log holds
every chat line as "[System] [CHAT] <text>". This script takes the first report of each kind and
rewrites the blocks between <!-- CHAT:check --> / <!-- CHAT:stressed --> markers, so the text next
to a screenshot is always the output of the same run.

Usage, from the project root, after the capture run:
  python3 -P branding/sync_chat.py [client log]   (default: build/run/clientGameTest/logs/latest.log)
"""

import html
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
CHAT = re.compile(r"\[System\] \[CHAT\] (.*)$")
HEAD = re.compile(r"^(.+?)( at -?\d+ -?\d+ -?\d+ is )(thriving|dormant|stressed)$")
HEALTH_COLOUR = {"thriving": "c-green", "dormant": "c-yellow", "stressed": "c-red"}
# Reasons that make a coral stressed are red in game; the other crosses (water kind, light) are yellow.
RED_REASONS = ("out of water", "magma", "frozen")
TARGETS = {"check": "thriving", "stressed": "stressed"}


def reports(log):
    """Every /reef check report in the log, in order: (health, [lines])."""
    lines = [m.group(1) for m in map(CHAT.search, log.splitlines()) if m]
    found = []
    i = 0
    while i < len(lines):
        head = HEAD.match(lines[i])
        if head:
            block = [lines[i]]
            i += 1
            while i < len(lines) and lines[i].startswith("  "):
                block.append(lines[i])
                i += 1
            found.append((head.group(3), block))
        else:
            i += 1
    return found


def span(cls, text):
    return f'<span class="{cls}">{html.escape(text, quote=False)}</span>'


def to_html(block):
    out = []
    name, middle, health = HEAD.match(block[0]).groups()
    out.append(span("c-aqua", name) + span("c-gray", middle) + f'<span class="{HEALTH_COLOUR[health]} b">{health}</span>')
    for n, line in enumerate(block[1:], start=1):
        mark, text = line[:4], line[4:]
        if n == len(block) - 1:
            out.append(span("c-white-i", line))
        elif mark == "  ✔ ":
            out.append(span("c-aqua" if text.startswith("an active conduit shelters") else "c-green", mark) + span("c-gray", text))
        elif mark == "  ✘ ":
            out.append(span("c-red" if text.startswith(RED_REASONS) else "c-yellow", mark) + span("c-gray", text))
        elif mark == "  + ":
            out.append(span("c-aqua", mark) + span("c-gray", text))
        else:
            out.append(span("c-gray", line))
    return "\n".join(f'          <p class="l">{line}</p>' for line in out)


def to_markdown(block):
    return "```\n" + "\n".join(block) + "\n```"


def replace(path, name, body):
    text = path.read_text()
    pattern = re.compile(rf"(<!-- CHAT:{name} -->\n).*?(\n[ \t]*<!-- /CHAT:{name} -->)", re.S)
    if not pattern.search(text):
        return False
    path.write_text(pattern.sub(lambda m: m.group(1) + body + m.group(2), text, count=1))
    return True


def main():
    log_path = Path(sys.argv[1]) if len(sys.argv) > 1 else ROOT / "build/run/clientGameTest/logs/latest.log"
    found = reports(log_path.read_text(errors="replace"))
    for name, health in TARGETS.items():
        block = next((b for h, b in found if h == health), None)
        if block is None:
            sys.exit(f"No {health} /reef check report in {log_path}")
        done = [p.name for p, body in ((ROOT / "site/index.html", to_html(block)),
                                       (ROOT / "branding/curseforge-description.md", to_markdown(block)))
                if replace(p, name, body)]
        print(f"CHAT:{name} ({block[0]}) -> {', '.join(done) or 'no markers found'}")


if __name__ == "__main__":
    main()
