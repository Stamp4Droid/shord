#!/usr/bin/env python

import argparse
import os
import sys
import util

parser = argparse.ArgumentParser()
parser.add_argument('path_file')
parser.add_argument('traces_dir', nargs='?')
args = parser.parse_args()

out = (sys.stdout if args.traces_dir is None else
       open(util.switch_dir(args.path_file, args.traces_dir, 'tr'), 'w'))
def print_step(step):
    out.write(str(step) + '\n')

tree = util.PathTree(args.path_file)
tree.walk(print_step)

if args.traces_dir is not None:
    out.close()
