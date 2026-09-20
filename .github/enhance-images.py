from pathlib import Path
import re
from PIL import Image, ImageEnhance, ImageFilter

PHOTO_FILES = [
    Path("realizace-hlavni.jpg"),
    *[Path(f"realizace-{i}.jpg") for i in range(1, 7)],
]

# Enhance the source assets only in the Pages build workspace.
# The repository originals stay untouched, so sharpening is never compounded.
for path in PHOTO_FILES:
    if not path.exists():
        continue
    with Image.open(path) as src:
        im = src.convert("RGB")
        # Gentle detail enhancement without the old median blur.
        im = ImageEnhance.Contrast(im).enhance(1.04)
        im = ImageEnhance.Color(im).enhance(1.03)
        im = im.filter(ImageFilter.UnsharpMask(radius=1.15, percent=165, threshold=2))
        im.save(path, "JPEG", quality=94, optimize=True, progressive=True)

p = Path("index.html")
s = p.read_text(encoding="utf-8")

# Remove the old SVG convolution filter and all references to it.
s = re.sub(r'<svg[^>]*>\s*(?:<defs>)?\s*<filter[^>]*id=["\']somsiSharpen["\'][\s\S]*?</filter>\s*(?:</defs>)?\s*</svg>', '', s, flags=re.I)
s = re.sub(r'filter:url\(#somsiSharpen\)\s*', '', s, flags=re.I)

# Use ordinary CSS presentation; no SVG sharpening fallback is needed.
s = re.sub(
    r'(\.card img\{[^}]*?)filter:[^;}]+;([^}]*\})',
    r'\1filter:contrast(1.04) saturate(1.03);\2',
    s,
    flags=re.S,
)
s = re.sub(
    r'(\.gallery img\{[^}]*?)filter:[^;}]+;([^}]*\})',
    r'\1filter:contrast(1.05) saturate(1.04);\2',
    s,
    flags=re.S,
)
s = re.sub(
    r'(\.split img\{[^}]*?)filter:[^;}]+;([^}]*\})',
    r'\1filter:contrast(1.04) saturate(1.03);\2',
    s,
    flags=re.S,
)

# Remove any previously generated final override block.
s = re.sub(r'<style id="somsi-photo-final">.*?</style>\s*', '', s, flags=re.S)

version = "20260920-photos-v2"
for name in [
    "realizace-1.jpg", "realizace-2.jpg", "realizace-3.jpg",
    "realizace-4.jpg", "realizace-5.jpg", "realizace-6.jpg",
    "realizace-hlavni.jpg",
]:
    # Replace an existing cache-busting version if present, otherwise add one.
    s = re.sub(rf'{re.escape(name)}(?:\?v=[^\"\') ]+)?', f'{name}?v={version}', s)

p.write_text(s, encoding="utf-8")
print("SOMSI live photo enhancement v2 applied.")
