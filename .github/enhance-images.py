from pathlib import Path

p = Path("index.html")
s = p.read_text(encoding="utf-8")

# Client-side photographic sharpening: keeps the original photos intact while
# improving perceived detail and contrast on the live site.
css = r'''\n/* SOMSI photo enhancement */\n.card img,.gallery img,.split img,.modal img{filter:url(#somsi-sharpen) saturate(1.04) contrast(1.03)}\n'''
if "#somsi-sharpen" not in s:
    s = s.replace("</style>", css + "</style>", 1)

svg = r'''\n<svg aria-hidden="true" width="0" height="0" style="position:absolute;overflow:hidden">\n  <defs>\n    <filter id="somsi-sharpen" x="-10%" y="-10%" width="120%" height="120%">\n      <feConvolveMatrix order="3" kernelMatrix="0 -0.8 0 -0.8 4.2 -0.8 0 -0.8 0" preserveAlpha="true"/>\n    </filter>\n  </defs>\n</svg>\n'''
if 'id="somsi-sharpen"' not in s:
    s = s.replace("<body>", "<body>" + svg, 1)

p.write_text(s, encoding="utf-8")
print("SOMSI photo enhancement applied to live build.")
