#!/usr/bin/env python

import argparse
import os
import re
import subprocess
import tempfile
import tgf2dot

# =============================================================================

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

def print_svg(svg_file, id_prefix):
    skip = 6
    header = 3
    for line in svg_file:
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
    svg_file.seek(0)

def print_html(graph_svg, pri_svg, sec_svgs, effects_fname, vmap_fname):
    print head
    print_svg(graph_svg, '0')
    print "</br>"
    print_svg(pri_svg, '1')
    print "</br>"
    for c in sec_svgs:
        print_svg(c, '2')
    print "</br>"
    for c in sec_svgs:
        print_svg(c, '3')
    print '<script>'
    with open(effects_fname) as fin:
        for line in fin:
            toks = line.split()
            print ('recordEffect("%s","%s","%s","%s","%s");' %
                   (toks[0], toks[1], toks[2], toks[3], ' '.join(toks[4:])))
    i = 0
    if os.path.exists(vmap_fname):
        with open(vmap_fname) as fin:
            for line in fin:
                print 'recordName("%s","%s");' % ('v%s' % i, line[:-1])
                i += 1
    print '</script>'
    print tail

# =============================================================================

def list_tgfs(rsm_dname):
    return [os.path.join(rsm_dname, tgf_fbase)
            for tgf_fbase in os.listdir(rsm_dname)
            if tgf_fbase.endswith('.tgf')]

def dot2svg(dot_file):
    svg_file = tempfile.TemporaryFile(suffix='svg')
    subprocess.check_call(['dot', '-Tsvg'], stdin=dot_file, stdout=svg_file)
    svg_file.seek(0)
    return svg_file

def tgf2svg(tgf_fname):
    with tempfile.TemporaryFile(suffix='dot') as dot_file:
        tgf2dot.convert(tgf_fname, dot_file)
        dot_file.seek(0)
        return dot2svg(dot_file)

# =============================================================================

parser = argparse.ArgumentParser()
parser.add_argument('pri_comp')
parser.add_argument('sec_dir')
parser.add_argument('app_out_dir')
parser.add_argument('start')
args = parser.parse_args()

with open(os.path.join(args.app_out_dir, 'dump',
                       args.start + '.dot')) as graph_dot:
    graph_svg = dot2svg(graph_dot)
pri_svg = tgf2svg(args.pri_comp)
sec_svgs = [tgf2svg(c) for c in list_tgfs(args.sec_dir)]
effects_fname = os.path.join(args.app_out_dir, 'dump', args.start + '.summs')
vmap_fname = os.path.join(args.app_out_dir, 'chord_output', 'bddbddb', 'V.map')
print_html(graph_svg, pri_svg, sec_svgs, effects_fname, vmap_fname)
