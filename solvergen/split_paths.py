#!/usr/bin/env python

import argparse
import os.path

parser = argparse.ArgumentParser()
parser.add_argument('paths_xml_file', type=argparse.FileType('r'))
parser.add_argument('out_dir')
args = parser.parse_args()

i = 0
outf = None
with open(args.paths_xml_file) as f:
    for line in f:
        if '<path>' in line:
            assert outf is None
            outf = open(os.path.join(args.out_dir, str(i) + '.xml'), 'w')
            outf.write(line)
        elif '</path>' in line:
            assert outf is not None
            outf.write(line)
            outf.close()
            outf = None
            i += 1
        elif outf is not None:
            outf.write(line)
assert outf is None
