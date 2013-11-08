#!/usr/bin/env python

import argparse
import sys
import xml.etree.ElementTree as ET

parser = argparse.ArgumentParser()
parser.add_argument('path_xml_file')
parser.add_argument('dat_file', type=argparse.FileType('r'))
args = parser.parse_args()

tree = ET.parse(args.path_xml_file)
root = tree.getroot()
assert root.tag == 'path'
assert len(list(root)) == 1
top_edge = root[0]
assert top_edge.tag == 'NTStep'
src = top_edge.attrib['from']
dst = top_edge.attrib['to']
index = top_edge.get('index')
to_find = '%s %s%s\n' % (src, dst, '' if index is None else (' %s' % index))

for line in args.dat_file:
    if line == to_find:
        sys.exit(0)
sys.exit(1)
