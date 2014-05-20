#!/usr/bin/env python

import argparse
import re

head = """<html>
<head>
<script src="scripts.js"></script>
</head>
<body>
<pre id="name"></pre>
<pre id="labels"></pre>
<pre id="effect"></pre>"""

style_decl = """<style>
g.edge>a:hover>polygon {
  stroke: red;
  fill: red;
}
g.edge>a:hover>path {
  stroke: red;
}
</style>"""

tail = """</body>
</html>"""

# =============================================================================

def print_svg(fname, id_prefix):
    skip = 6
    header = 3
    with open(fname) as f:
        for line in f:
            if skip > 0:
                skip -= 1
                continue
            if header > 0:
                header -= 1
                if header == 0:
                    print style_decl
            if id_prefix is not None:
                line = re.sub(r'id="([^\"]+)"',
                              r'id="%s::\1"' % id_prefix, line)
            print line[:-1]

# =============================================================================

parser = argparse.ArgumentParser()
parser.add_argument('v_map', type=argparse.FileType('r'))
parser.add_argument('effects', type=argparse.FileType('r'))
parser.add_argument('graph')
parser.add_argument('pri_comp')
parser.add_argument('sec_comps', nargs="+")
args = parser.parse_args()

print head
print_svg(args.graph, '0')
print "</br>"
print_svg(args.pri_comp, '1')
print "</br>"
for c in args.sec_comps:
    print_svg(c, '2')
print "</br>"
for c in args.sec_comps:
    print_svg(c, '3')
print '<script>'
for line in args.effects:
    toks = line.split()
    print ('recordEffect("%s","%s","%s","%s","%s");' %
           (toks[0], toks[1], toks[2], toks[3], ' '.join(toks[4:])))
i = 0
for line in args.v_map:
    print 'recordName("%s","%s");' % ('v%s' % i, line[:-1])
    i += 1
print '</script>'
print tail
