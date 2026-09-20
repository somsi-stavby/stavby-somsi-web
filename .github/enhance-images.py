from pathlib import Path
import re

p = Path("index.html")
s = p.read_text(encoding="utf-8")

# Final live-photo presentation: match the sharp reference composition on mobile,
# avoid the previous SVG filter fallback, and bust CDN/browser image cache.
css = '''
<style id="somsi-photo-final">
/* SOMSI – final photo presentation */
.card img,
.gallery img,
.split img,
.modal img {
  filter: contrast(1.10) saturate(1.08) brightness(1.02) !important;
  image-rendering: auto;
  object-position: center center;
  -webkit-backface-visibility: hidden;
  backface-visibility: hidden;
}
.card img { height: 185px !important; }
@media (max-width:560px) {
  .card img { height: 138px !important; }
}
</style>
'''

# Replace any previous final override so deployment is deterministic.
s = re.sub(r'<style id="somsi-photo-final">.*?</style>\s*', '', s, flags=re.S)
s = s.replace('</style>', css + '</style>', 1)

# Force the live site/CDN to request the current photo assets again.
version = '20260920-photos-final'
for name in ['realizace-1.jpg','realizace-2.jpg','realizace-3.jpg','realizace-4.jpg','realizace-5.jpg','realizace-6.jpg','realizace-hlavni.jpg']:
    s = s.replace(f'src="{name}"', f'src="{name}?v={version}"')
    s = s.replace(f'url("{name}")', f'url("{name}?v={version}")')

p.write_text(s, encoding="utf-8")
print("SOMSI final live photo presentation applied.")
