"""Builds Play Store phone screenshots (1080x1920) and the feature graphic (1024x500).

Screens are real captures from a Galaxy Z Flip4 (1080x2640). The system status and navigation
bars are cropped off; the app UI itself is not altered.
"""
import sys
from PIL import Image, ImageDraw, ImageFilter, ImageFont

FONT = '/tmp/hueno/fonts/Roboto-Regular.ttf'
INK = (18, 20, 26)
ON_INK = (244, 244, 242)
MUTED = (169, 175, 189)

def font(size, weight=b'Bold'):
    f = ImageFont.truetype(FONT, size)
    f.set_variation_by_name(weight)
    return f

def background(w, h):
    """Brand ink with a faint lift toward the top, so the phone stands out."""
    bg = Image.new('RGB', (w, h), INK)
    glow = Image.new('L', (w, h), 0)
    ImageDraw.Draw(glow).ellipse((-w * 0.3, -h * 0.35, w * 1.3, h * 0.55), fill=60)
    glow = glow.filter(ImageFilter.GaussianBlur(w // 6))
    lift = Image.new('RGB', (w, h), (40, 46, 62))
    return Image.composite(lift, bg, glow)

def crop_screen(path, top=120, bottom=2495):
    im = Image.open(path).convert('RGB')
    return im.crop((0, top, im.width, bottom))

def phone(screen, height, radius=54, bezel=14):
    """Screen inside a slim dark bezel with rounded corners and a soft shadow."""
    s = screen.resize((round(screen.width * height / screen.height), height), Image.LANCZOS)
    w, h = s.width + 2 * bezel, s.height + 2 * bezel
    body = Image.new('RGBA', (w, h), (0, 0, 0, 0))
    ImageDraw.Draw(body).rounded_rectangle((0, 0, w - 1, h - 1), radius + bezel, fill=(8, 9, 12, 255),
                                           outline=(70, 76, 92, 255), width=2)
    mask = Image.new('L', s.size, 0)
    ImageDraw.Draw(mask).rounded_rectangle((0, 0, s.width - 1, s.height - 1), radius, fill=255)
    body.paste(s, (bezel, bezel), mask)
    return body

def paste_with_shadow(canvas, item, xy, blur=40, offset=24, alpha=150):
    sh = Image.new('L', (item.width + blur * 4, item.height + blur * 4), 0)
    ImageDraw.Draw(sh).rounded_rectangle((blur * 2, blur * 2 + offset, blur * 2 + item.width, blur * 2 + item.height + offset),
                                         80, fill=alpha)
    sh = sh.filter(ImageFilter.GaussianBlur(blur))
    canvas.paste((0, 0, 0), (xy[0] - blur * 2, xy[1] - blur * 2), sh)
    canvas.paste(item, xy, item)

def wrap(draw, text, fnt, max_w):
    words, lines, line = text.split(), [], ''
    for w in words:
        trial = (line + ' ' + w).strip()
        if draw.textlength(trial, font=fnt) <= max_w:
            line = trial
        else:
            lines.append(line); line = w
    lines.append(line)
    return lines

def screenshot(src, caption, out):
    W, H = 1080, 1920
    c = background(W, H)
    d = ImageDraw.Draw(c)
    f = font(76)
    # A '|' in the caption marks a chosen line break, for balanced two-line captions.
    lines = [ln for part in caption.split('|') for ln in wrap(d, part.strip(), f, W - 160)]
    y = 120
    for ln in lines:
        d.text((W // 2, y), ln, font=f, fill=ON_INK, anchor='ma')
        y += 92
    ph = phone(crop_screen(src), H - y - 70)
    paste_with_shadow(c, ph, ((W - ph.width) // 2, y + 46))
    c.convert('RGB').save(out, optimize=True)

def feature(icon_svg_png, screen, out):
    W, H = 1024, 500
    c = background(W, H)
    d = ImageDraw.Draw(c)
    icon = Image.open(icon_svg_png).convert('RGBA').resize((132, 132), Image.LANCZOS)
    m = Image.new('L', icon.size, 0); ImageDraw.Draw(m).rounded_rectangle((0, 0, 131, 131), 30, fill=255)
    c.paste(icon, (72, 96), m)
    d.text((72, 258), 'Hueno', font=font(76), fill=ON_INK)
    d.text((72, 350), 'Point. Know the color.', font=font(40, b'Medium'), fill=ON_INK)
    d.text((72, 404), 'Color names you already know.', font=font(30, b'Regular'), fill=MUTED)
    # The top of the camera screen: app bar, viewfinder and the color name.
    shot = crop_screen(screen, top=120, bottom=1720)
    ph = phone(shot, 560, radius=44, bezel=12)
    paste_with_shadow(c, ph, (W - ph.width - 70, 58), blur=30, offset=18)
    c = c.convert('RGB')
    c.save(out, optimize=True)

if __name__ == '__main__':
    kind = sys.argv[1]
    if kind == 'shot':
        screenshot(sys.argv[2], sys.argv[3], sys.argv[4])
    else:
        feature(sys.argv[2], sys.argv[3], sys.argv[4])
