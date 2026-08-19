# ATS CV template

`ATS-TEMPLATE-V1` is backend-controlled and single-column. `LatexCvRenderer` owns all dynamic
content and escaping. Raw LaTeX is never accepted from or returned to clients.

The runtime compiler is XeLaTeX with shell escape disabled. Keep required packages minimal and do
not add parser-hostile graphical layout, charts, columns, or image-based text.
