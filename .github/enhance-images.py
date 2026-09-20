from pathlib import Path

p = Path("index.html")
s = p.read_text(encoding="utf-8")

# Improve perceived detail on all site photography without changing the layout.
css = '''
/* SOMSI photo enhancement */
.card img,
.gallery img,
.split img,
.modal img {
  filter: url(#somsi-sharpen) saturate(1.04) contrast(1.03);
}
'''
if "#somsi-sharpen" not in s:
    s = s.replace("</style>", css + "</style>", 1)

svg = '''
<svg aria-hidden="true" width="0" height="0" style="position:absolute;overflow:hidden">
  <defs>
    <filter id="somsi-sharpen" x="-10%" y="-10%" width="120%" height="120%">
      <feConvolveMatrix order="3" kernelMatrix="0 -0.35 0 -0.35 2.4 -0.35 0 -0.35 0" preserveAlpha="true"/>
    </filter>
  </defs>
</svg>
'''
if 'id="somsi-sharpen"' not in s:
    s = s.replace("<body>", "<body>" + svg, 1)

p.write_text(s, encoding="utf-8")
print("SOMSI photo enhancement applied to live build.")
