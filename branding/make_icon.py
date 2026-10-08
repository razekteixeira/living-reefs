"""Living Reefs mod icon.

Authored as a 32x32 pixel grid and exported with nearest-neighbour scaling only,
so every output stays crisp pixel art: a branching coral with a horn coral fan and
a tube coral clump on a sandy seabed, under a deep ocean tile with rising bubbles.

Run from the project root:  python3 -P branding/make_icon.py
(-P keeps the script directory off sys.path but still sees user-site Pillow.)
"""

from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
N = 32

PALETTE = {
    ".": (0, 0, 0, 0),            # transparent (outside rounded corners)
    "K": (4, 14, 24, 255),        # tile border
    "1": (18, 92, 112, 255),      # water, top band
    "2": (13, 70, 96, 255),       # water
    "3": (10, 52, 80, 255),       # water
    "4": (8, 38, 64, 255),        # water, deep
    "c": (64, 160, 170, 255),     # caustic light
    "O": (34, 8, 24, 255),        # object outline
    "P": (255, 104, 124, 255),    # coral pink
    "Q": (255, 176, 168, 255),    # coral highlight and polyp tips
    "p": (196, 52, 92, 255),      # coral shade
    "Y": (255, 214, 64, 255),     # horn coral yellow
    "y": (204, 150, 28, 255),     # horn coral shade
    "Z": (255, 240, 150, 255),    # horn coral highlight
    "V": (150, 92, 230, 255),     # tube coral violet
    "v": (98, 56, 170, 255),      # tube coral shade
    "W": (206, 170, 255, 255),    # tube coral rim
    "S": (222, 200, 140, 255),    # sand
    "s": (184, 158, 102, 255),    # sand shade
    "R": (90, 98, 104, 255),      # rock
    "B": (190, 236, 244, 255),    # bubble
}

# Branching coral: polylines on the 32 grid, stroked 2 px wide.
BRANCHES = [
    [(15, 27), (15, 19), (16, 12), (14, 9)],
    [(15, 22), (11, 18), (10, 12)],
    [(10, 15), (7, 12)],
    [(16, 18), (21, 13), (21, 8)],
    [(21, 11), (24, 8)],
]
TIPS = [(14, 9), (10, 12), (7, 12), (21, 8), (24, 8)]
TUBES = [(4, 22), (6, 20), (8, 23)]  # (x, top y) of each 2 px wide tube
FAN_CENTRE, FAN_RADIUS = (25, 25), 5
BUBBLES = [(26, 3), (28, 6), (24, 6)]


def blank():
    return [["." for _ in range(N)] for _ in range(N)]


def tile(grid):
    """Rounded tile: water bands getting darker with depth, a few caustic glints at the top."""
    corner = {(0, 0), (1, 0), (0, 1), (N - 1, 0), (N - 2, 0), (N - 1, 1),
              (0, N - 1), (1, N - 1), (0, N - 2), (N - 1, N - 1),
              (N - 2, N - 1), (N - 1, N - 2)}
    border = {(1, 1), (N - 2, 1), (1, N - 2), (N - 2, N - 2)}
    for y in range(N):
        for x in range(N):
            if (x, y) in corner:
                continue
            if x in (0, N - 1) or y in (0, N - 1) or (x, y) in border:
                grid[y][x] = "K"
            else:
                grid[y][x] = "1" if y < 7 else "2" if y < 14 else "3" if y < 21 else "4"
    for x, y in ((3, 2), (4, 2), (8, 3), (9, 3), (10, 3), (17, 2), (18, 2), (12, 5), (13, 5), (5, 5)):
        grid[y][x] = "c"


def seabed(grid):
    for y in range(27, N - 1):
        for x in range(1, N - 1):
            if grid[y][x] != "K":
                grid[y][x] = "s" if (x * 7 + y * 3) % 9 == 0 else "S"
    for x, y in ((2, 27), (3, 27), (2, 28), (19, 27), (20, 27), (29, 27), (29, 28)):
        grid[y][x] = "R"


def line_pixels(pts):
    out = []
    for (x0, y0), (x1, y1) in zip(pts, pts[1:]):
        steps = max(abs(x1 - x0), abs(y1 - y0))
        for t in range(steps + 1):
            x = x0 + round((x1 - x0) * t / steps)
            y = y0 + round((y1 - y0) * t / steps)
            if (x, y) not in out:
                out.append((x, y))
    return out


def outline(grid, shape, keep=None):
    keep = keep or set()
    for x, y in shape:
        for dx in (-1, 0, 1):
            for dy in (-1, 0, 1):
                p = (x + dx, y + dy)
                if p not in shape and p not in keep and 1 < p[0] < N - 2 and 1 < p[1] < 27:
                    grid[p[1]][p[0]] = "O"


def shade(grid, shape, base, light, dark):
    for x, y in shape:
        grid[y][x] = base
    for x, y in shape:
        if (x - 1, y) not in shape or (x, y - 1) not in shape:
            grid[y][x] = light
        elif (x + 1, y) not in shape:
            grid[y][x] = dark


def tubes(grid):
    shape = set()
    for x, top in TUBES:
        for y in range(top, 27):
            shape.add((x, y))
            shape.add((x + 1, y))
    outline(grid, shape)
    left_columns = {x for x, _ in TUBES}
    for x, y in shape:
        grid[y][x] = "V" if x in left_columns else "v"
    for x, top in TUBES:
        grid[top][x] = "W"
        grid[top][x + 1] = "W"
    return shape


def fan(grid, avoid):
    cx, cy = FAN_CENTRE
    shape = set()
    for y in range(cy - FAN_RADIUS, cy + 2):
        for x in range(cx - FAN_RADIUS, cx + FAN_RADIUS + 1):
            if (x - cx) ** 2 + (y - cy) ** 2 <= FAN_RADIUS ** 2 + 2 and y < 27 and x < N - 3:
                shape.add((x, y))
    outline(grid, shape, avoid)
    shade(grid, shape, "Y", "Z", "y")
    # Ridges radiating from the stalk.
    for x, y in shape:
        if (x - cx) % 2 == 0 and y < cy - 1 and (x, y) in shape:
            grid[y][x] = "y" if grid[y][x] == "Y" else grid[y][x]
    return shape


def branching(grid):
    body = set()
    for branch in BRANCHES:
        for x, y in line_pixels(branch):
            body.add((x, y))
            body.add((x + 1, y))
    for x, y in TIPS:
        body.update({(x, y - 1), (x + 1, y - 1)})
    outline(grid, body)
    # Light comes from the upper left: left edges pink, right edges shaded, tops catch the light.
    for x, y in body:
        if (x + 1, y) not in body:
            grid[y][x] = "p"
        elif (x, y - 1) not in body and (x - 1, y) not in body:
            grid[y][x] = "Q"
        else:
            grid[y][x] = "P"
    for x, y in TIPS:
        grid[y - 1][x] = "Q"
        grid[y - 1][x + 1] = "Q"
    return body


def bubbles(grid):
    for x, y in BUBBLES:
        grid[y][x] = "B"
    grid[2][27] = "B"


def render(grid):
    img = Image.new("RGBA", (N, N))
    for y in range(N):
        for x in range(N):
            img.putpixel((x, y), PALETTE[grid[y][x]])
    return img


def main():
    grid = blank()
    tile(grid)
    seabed(grid)
    tube_shape = tubes(grid)
    fan_shape = fan(grid, tube_shape)
    branching(grid)
    bubbles(grid)
    img = render(grid)

    outputs = {
        ROOT / "src/main/resources/assets/livingreefs/icon.png": 128,
        ROOT / "branding/icon-512.png": 512,
        ROOT / "branding/icon-32.png": 32,
        ROOT / "site/favicon.png": 64,
    }
    for path, size in outputs.items():
        path.parent.mkdir(parents=True, exist_ok=True)
        img.resize((size, size), Image.NEAREST).save(path, optimize=True)
        print(f"wrote {path.relative_to(ROOT)} ({size}x{size})")


if __name__ == "__main__":
    main()
