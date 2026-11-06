#!/usr/bin/env python3
"""Build Vanim's Android launcher resources from docs/brand/vanim-source-icon.jpg."""
from pathlib import Path
import sys
from PIL import Image, ImageDraw, ImageOps
ROOT = Path(__file__).resolve().parents[1]
RES = Path(sys.argv[1]) if len(sys.argv) > 1 else ROOT / "app/src/main/res"
SOURCE = ROOT / "docs/brand/vanim-source-icon.jpg"
BRAND = ROOT / "docs/brand"
SIZES = {"mipmap-mdpi": 48, "mipmap-hdpi": 72, "mipmap-xhdpi": 96, "mipmap-xxhdpi": 144, "mipmap-xxxhdpi": 192}
def square_source(size: int) -> Image.Image:
    image = Image.open(SOURCE).convert("RGB")
    return ImageOps.pad(image, (size, size), method=Image.Resampling.LANCZOS, color=(0, 0, 0), centering=(0.5, 0.5))
def alpha_foreground(size: int) -> Image.Image:
    rgb = square_source(size)
    px, out = rgb.load(), Image.new("RGBA", (size, size), (0, 0, 0, 0))
    dst = out.load()
    for y in range(size):
        for x in range(size):
            r, g, b = px[x, y]
            peak = max(r, g, b)
            if peak > 9:
                dst[x, y] = (min(255, r * 255 // peak), min(255, g * 255 // peak), min(255, b * 255 // peak), peak)
    return out
def save_round(size: int, output: Path) -> None:
    icon = square_source(size).convert("RGBA")
    mask = Image.new("L", (size, size), 0)
    ImageDraw.Draw(mask).ellipse((0, 0, size - 1, size - 1), fill=255)
    icon.putalpha(mask)
    icon.save(output)
def main() -> None:
    (RES / "drawable-nodpi").mkdir(parents=True, exist_ok=True)
    foreground = alpha_foreground(432)
    foreground.save(RES / "drawable-nodpi/ic_launcher_foreground.png")
    mono = Image.new("RGBA", foreground.size, (255, 255, 255, 0))
    mono.putalpha(foreground.getchannel("A"))
    mono.save(RES / "drawable-nodpi/ic_launcher_monochrome.png")
    for folder, size in SIZES.items():
        target = RES / folder
        target.mkdir(parents=True, exist_ok=True)
        square_source(size).save(target / "ic_launcher.png", optimize=True)
        save_round(size, target / "ic_launcher_round.png")
    BRAND.mkdir(parents=True, exist_ok=True)
    square_source(512).save(BRAND / "icon-512.png", optimize=True)
    save_round(512, BRAND / "icon-round-preview.png")
    print("Vanim launcher assets generated from", SOURCE)
if __name__ == "__main__": main()
