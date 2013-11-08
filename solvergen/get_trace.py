#!/usr/bin/env python

import argparse
import xml.etree.ElementTree as ET

def is_terminal_step(node):
    if node.tag != 'NTStep' and node.tag != 'TempStep':
        assert list(node) == []
        assert node.tag[0] >= 'a' and node.tag[0] <= 'z'
        return True
    return False

def print_step(node, reverse):
    index = node.get('index')
    print ('%s %s %s %s%s' %
           ('REV' if reverse else 'STR', node.tag,
            node.attrib['from'], node.attrib['to'],
            '' if index is None else (' ' + index)))

def process(node, reverse):
    reverse = reverse ^ (node.attrib['reverse'] == 'true')
    if is_terminal_step(node):
        print_step(node, reverse)
    for child in (reversed(node) if reverse else node):
        process(child, reverse)

parser = argparse.ArgumentParser()
parser.add_argument('path_xml_file')
args = parser.parse_args()

tree = ET.parse(args.path_xml_file)
root = tree.getroot()
assert root.tag == 'path'
for top_child in root:
    process(top_child, False)
