#!/usr/bin/env python

import argparse
import os
from os.path import join, basename, splitext
import re
import subprocess
import tempfile
import tgf2dot

# =============================================================================

style_decl = """<style>
g.edge>a:hover>polygon {
  stroke: red;
  fill: red;
}
g.edge>a:hover>path {
  stroke: red;
}
</style>"""

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

def print_html(start, graph_svg, pri_name, pri_svg, sec_names, sec_svgs,
               effects_fname, vmap_fname):
    print """<html>
<head>
<script src="scripts.js"></script>
</head>
<body>

<div style="padding-left:10px;width:600px;height:2000px;float:right;">
<p>Source-code location of selected node:</p>
<pre id="name">-</pre>
<p>Label of selected edge:</p>
<pre id="labels">-</pre>
<p><b>5: Possible effects of selected path on the secondary matching
stack:</b></p>
<pre id="effect">-</pre>
</div>

<div style="padding-right:10px;">

<p>This webpage visualizes the summarization process over a specific part of
the program, represented as a data flow graph. Each node on this graph
represents a variable or intermediate result in the code. Edges represent basic
language operations that cause flow of data between program values, and are
labeled according to the exact kind of operation they represent (e.g.
assignment, field load, field store).</p>

<p>The nodes shown are all values which can be reached starting from the entry
point "%s" (marked with an octagon) without escaping the summarization
boundaries [method boundaries]. Each entry point to a summarization subgraph
[i.e. each formal parameter, plus the return variable] is handled separately
(this particular webpage corresponds specifically to %s). Values through which
data can exit the subgraph are marked with a double border (the same node can
be both an entry and an exit point). The summarization boundaries are chosen to
match with some recursive component call of the primary RSM [in this case, the
primary RSM expresses call matching, thus the boundaries are all call and
return edges].</p>

<p>The overall analysis is path based, i.e. we're interested in finding
end-to-end paths on the whole-program graph, which form a valid sentence on
each of the RSMs comporising the analysis specification. The summarization
process will enumerate all the paths which start from the
subgraph entry point, stay within the subgraph, and end at some exit point. The
goal of this process is to enumerate all nodes which can be reached from the
entry point through a path which can be used to complete a recursive call of
some component in the primary RSM [in this case, all the recursive calls on the
primary RSM are to the "IntraFun" component, which accepts only well-balanced
call paths]. Then, when performing the final, top-down propagation, we don't
have to recurse into the primary RSM's sub-components, instead we can use
the set of pre-calculated summaries for each component.</p>

<p>The summaries we calculate will represent only well-balanced paths over some
component of the primary RSM. Unfortuantely, those same paths are not
necessarily well-balanced on the other (secondary) RSM of the analysis.
Therefore, we have to keep track of how each path represented through our
summary could affect the matching state of the secondary RSM (for each starting
state, what states could be reached by following a path through the subgraph
being summarized). The matching algorithm over a RSM includes a stack,
therefore we also need to consider how each path could affect that stack as
well.</p>

<p>The red superscript over each node name is the number of different effects
which a path reaching that node (starting from the entry point) can have over
the secondary RSM.</p>""" % (start, start)
    print """<hr/>
<p><b>1: Pick a path target on the summarization subgraph.</b></p>
<p>The starting node is always the entry point.</p>"""
    print_svg(graph_svg, '0')
    print '<hr/>'
    print """<p><b>2: Pick a target state on the primary RSM component being
summarized, %s.</b></p>
<p>The starting state is always the component's start state, marked with an
octagon. Only paths reaching an exit state, marked with a double border, will
be included in the summary.</p>""" % pri_name
    print_svg(pri_svg, '1')
    print '<hr/>'
    print """<p><b>3: Pick a starting state on some component of the secondary
RSM.</b></p>"""
    for (n,c) in zip(sec_names, sec_svgs):
        print n + ':'
        print '<br/>'
        print_svg(c, '2')
        print '<br/>'
    print '<hr/>'
    print """<p><b>4: Pick a target state on some component of the secondary
RSM.</b></p>"""
    for (n,c) in zip(sec_names, sec_svgs):
        print n + ':'
        print '<br/>'
        print_svg(c, '3')
        print '<br/>'
    print """</div>
<script>"""
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
    print """</script>
</body>
</html>"""

# =============================================================================

def list_tgfs(rsm_dname):
    return [join(rsm_dname, tgf_fbase)
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
parser.add_argument('dump_dir')
parser.add_argument('dom_maps_dir')
parser.add_argument('start')
args = parser.parse_args()

with open(join(args.dump_dir, args.start + '.dot')) as graph_dot:
    graph_svg = dot2svg(graph_dot)
pri_name = splitext(splitext(basename(args.pri_comp))[0])[0]
pri_svg = tgf2svg(args.pri_comp)
sec_names = [splitext(splitext(basename(c))[0])[0]
             for c in list_tgfs(args.sec_dir)]
sec_svgs = [tgf2svg(c) for c in list_tgfs(args.sec_dir)]
effects_fname = join(args.dump_dir, args.start + '.summs')
vmap_fname = join(args.dom_maps_dir, 'V.map')
print_html(args.start, graph_svg, pri_name, pri_svg, sec_names, sec_svgs,
           effects_fname, vmap_fname)
