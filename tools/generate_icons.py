#!/usr/bin/env python3
"""Generate launcher icons (PNG, all densities) using only the Python stdlib.

Artwork: gold crescent + star on a deep emerald background.
Run from the repository root:  python3 tools/generate_icons.py
"""
import math
import os
import struct
import zlib

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES_DIR = os.path.join(REPO_ROOT, "app", "src", "main", "res")

# Density -> icon size in px
DENSITIES = {
    "mdpi": 48,
    "hdpi": 72,
    "xhdpi": 96,
    "xxhdpi": 144,
    "xxxhdpi": 192,
}

BG = (11, 107, 58)      # deep emerald
GOLD = (232, 184, 75)    # gold
SS = 4                   # supersampling factor for smooth edges


def write_png(path, pixels, size):
    """Write RGBA pixels (list of rows of (r,g,b,a) tuples) as a PNG file."""
    raw = b"".join(
        b"\x00" + b"".join(struct.pack(">4B", *px) for px in row)
        for row in pixels
    )

    def chunk(ctype, data):
        c = ctype + data
        return struct.pack(">I", len(data)) + c + struct.pack(">I", zlib.crc32(c))

    png = (
        b"\x89PNG\r\n\x1a\n"
        + chunk(b"IHDR", struct.pack(">IIBBBBB", size, size, 8, 6, 0, 0, 0))
        + chunk(b"IDAT", zlib.compress(raw, 9))
        + chunk(b"IEND", b"")
    )
    with open(path, "wb") as f:
        f.write(png)


def render(size, round_icon):
    """Render the crescent + star artwork at the given size."""
    cx, cy = size / 2.0, size / 2.0

    def in_circle(px, py, ccx, ccy, r):
        return (px - ccx) ** 2 + (py - ccy) ** 2 <= r * r

    # Crescent: big disc minus offset disc (normalized coords, icon = 1x1)
    big = (0.46 * size, 0.52 * size, 0.235 * size)
    cut = (0.565 * size, 0.445 * size, 0.195 * size)
    # 5-point star
    sx, sy, sR, sr = 0.665 * size, 0.345 * size, 0.085 * size, 0.036 * size
    star_pts = []
    for i in range(10):
        ang = math.radians(-90 + i * 36)
        r = sR if i % 2 == 0 else sr
        star_pts.append((sx + r * math.cos(ang), sy + r * math.sin(ang)))

    def in_poly(px, py, pts):
        inside = False
        n = len(pts)
        j = n - 1
        for i in range(n):
            xi, yi = pts[i]
            xj, yj = pts[j]
            if (yi > py) != (yj > py) and px < (xj - xi) * (py - yi) / (yj - yi) + xi:
                inside = not inside
            j = i
        return inside

    rows = []
    for y in range(size):
        row = []
        for x in range(size):
            gold_hits = 0
            total = SS * SS
            for oy in range(SS):
                for ox in range(SS):
                    px = x + (ox + 0.5) / SS
                    py = y + (oy + 0.5) / SS
                    in_crescent = in_circle(px, py, *big) and not in_circle(px, py, *cut)
                    if in_crescent or in_poly(px, py, star_pts):
                        gold_hits += 1
            t = gold_hits / total
            r = round(BG[0] + (GOLD[0] - BG[0]) * t)
            g = round(BG[1] + (GOLD[1] - BG[1]) * t)
            b = round(BG[2] + (GOLD[2] - BG[2]) * t)
            a = 255
            if round_icon:
                # Circular mask with 1px feather
                d = math.hypot(x + 0.5 - cx, y + 0.5 - cy)
                if d >= size / 2:
                    r, g, b, a = 0, 0, 0, 0
                elif d > size / 2 - 1:
                    a = round(255 * (size / 2 - d))
            row.append((r, g, b, a))
        rows.append(row)
    return rows


def main():
    for density, size in DENSITIES.items():
        d = os.path.join(RES_DIR, f"mipmap-{density}")
        os.makedirs(d, exist_ok=True)
        write_png(os.path.join(d, "ic_launcher.png"), render(size, False), size)
        write_png(os.path.join(d, "ic_launcher_round.png"), render(size, True), size)
        print(f"mipmap-{density}: {size}x{size} (square + round)")


if __name__ == "__main__":
    main()
