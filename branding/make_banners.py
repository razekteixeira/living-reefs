"""Composes the project banners from real captures and the rendered 3D logo.

Inputs (run branding/make_media.sh and branding/render_logo3d.py first):
  site/media/gallery-restored.png  background, a real underwater shot of a restored reef
  site/media/check.png             the chat panel is cropped from this real capture
  branding/logo3d.png              Blender render of the icon

Outputs:
  site/media/banner.png         1920x1080, CurseForge gallery and description header
  site/media/social.png         1280x640, GitHub social preview and link previews (og:image)
  site/media/readme-header.png  1600x480, top of the README

Run from the project root: python3 -P branding/make_banners.py
"""

from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter, ImageFont

ROOT = Path(__file__).resolve().parent.parent
MEDIA = ROOT / "site" / "media"
FONTS = ROOT / "branding" / "fonts"
CORAL = (255, 118, 128)
CORAL_SHADOW = (120, 30, 60)
INK = (4, 18, 30)


def font(name, size, weight=None):
    f = ImageFont.truetype(str(FONTS / name), size)
    if weight is not None:
        f.set_variation_by_axes([weight])
    return f


def cover(image, size):
    """Scale and centre-crop to fill size."""
    w, h = size
    scale = max(w / image.width, h / image.height)
    resized = image.resize((round(image.width * scale), round(image.height * scale)), Image.LANCZOS)
    left = (resized.width - w) // 2
    top = (resized.height - h) // 2
    return resized.crop((left, top, left + w, top + h))


def background(size):
    reef = Image.open(MEDIA / "gallery-restored.png").convert("RGB")
    bg = cover(reef, size).filter(ImageFilter.GaussianBlur(1.0))
    # Deep water towards the left, where the text sits.
    shade = Image.new("L", size)
    px = shade.load()
    for x in range(size[0]):
        alpha = int(238 - 190 * min(1.0, x / (size[0] * 0.8)))
        for y in range(size[1]):
            px[x, y] = alpha
    return Image.composite(Image.new("RGB", size, INK), bg, shade).convert("RGBA")


def chat_panel():
    """The real /reef check report, cropped from the capture (bottom left, above the input line)."""
    check = Image.open(MEDIA / "check.png").convert("RGBA")
    scale = check.width / 1280
    box = tuple(round(v * scale) for v in (0, 494, 668, 640))
    return check.crop(box)


def logo(height):
    image = Image.open(ROOT / "branding" / "logo3d.png").convert("RGBA")
    bbox = image.getbbox()
    image = image.crop(bbox)
    width = round(image.width * height / image.height)
    image = image.resize((width, height), Image.LANCZOS)
    pad = height // 6
    size = (width + 2 * pad, height + 2 * pad)
    # Blur on the padded canvas so the glow fades out instead of stopping at the image edge.
    mask = Image.new("L", size)
    mask.paste(image.getchannel("A").point(lambda a: a // 3), (pad, pad))
    glow = Image.new("RGBA", size, (90, 220, 230, 0))
    glow.putalpha(mask.filter(ImageFilter.GaussianBlur(height // 12)))
    glow.alpha_composite(image, (pad, pad))
    return glow


def title(draw, xy, size, max_width):
    x, y = xy
    text = "LIVING REEFS"
    f = font("Silkscreen-Bold.ttf", size)
    while draw.textlength(text, font=f) > max_width and size > 20:
        size -= 2
        f = font("Silkscreen-Bold.ttf", size)
    shadow = max(3, size // 24)
    draw.text((x + shadow, y + shadow), text, font=f, fill=CORAL_SHADOW)
    draw.text((x, y), text, font=f, fill=CORAL)
    return draw.textbbox((x, y), text, font=f)[3]


def compose(size, out, title_size, tag_size, show_chat, logo_height):
    w, h = size
    canvas = background(size)
    badge = logo(logo_height)
    margin = round(w * 0.065)
    text_width = w - badge.width - margin - round(w * 0.01)
    draw = ImageDraw.Draw(canvas)
    y = round(h * (0.14 if show_chat else 0.22))
    draw.text((margin, y), "FABRIC  ·  MINECRAFT 26.3  ·  SERVER-SIDE", font=font("Geist.ttf", round(tag_size * 0.55), 600),
              fill=(150, 196, 206))
    y = title(draw, (margin, y + round(tag_size * 0.95)), title_size, text_width) + round(tag_size * 0.5)
    for line in ("Coral reefs that grow, bleach", "and recover on their own."):
        draw.text((margin, y), line, font=font("Geist.ttf", tag_size, 560), fill=(236, 244, 246))
        y += round(tag_size * 1.22)
    if show_chat:
        panel = chat_panel()
        target_w = round(w * 0.42)
        panel = panel.resize((target_w, round(panel.height * target_w / panel.width)), Image.NEAREST)
        y += round(tag_size * 0.6)
        frame = Image.new("RGBA", (panel.width + 8, panel.height + 8), CORAL + (255,))
        canvas.alpha_composite(frame, (margin - 4, y - 4))
        canvas.alpha_composite(panel, (margin, y))
    canvas.alpha_composite(badge, (w - badge.width + round(badge.height * 0.06), (h - badge.height) // 2))
    canvas.convert("RGB").save(out, optimize=True)
    print("wrote", out.relative_to(ROOT), canvas.size)


compose((1920, 1080), MEDIA / "banner.png", title_size=140, tag_size=54, show_chat=True, logo_height=700)
compose((1280, 640), MEDIA / "social.png", title_size=96, tag_size=38, show_chat=False, logo_height=440)
compose((1600, 480), MEDIA / "readme-header.png", title_size=88, tag_size=34, show_chat=False, logo_height=360)
