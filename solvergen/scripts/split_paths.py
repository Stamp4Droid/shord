#!/usr/bin/env python

import argparse
import os
import re
import util

State = util.enum('OUT_OF_EDGE', 'IN_EDGE', 'IN_PATH')

class ScannerFSM(util.BaseClass):
    def __init__(self, symbol, out_dir):
        self._state = State.OUT_OF_EDGE
        self._symbol = symbol
        self._out_dir = out_dir
        self._edge = None
        self._paths_per_edge = {}
        self._fout = None

    def process(self, line):
        if line.startswith('<edge'):
            pat = r"^<edge from='(\w+)' to='(\w+)'(?: index='([0-9]+)')?>$"
            m  = re.match(pat, line)
            self._enter_edge(m.group(1), m.group(2), m.group(3))
        elif line == '<path>\n':
            self._enter_path()
            self._fout.write(line)
        elif line == '</path>\n':
            self._fout.write(line)
            self._exit_path()
        elif line == '</edge>\n':
            self._exit_edge()
        elif self._state == State.IN_PATH:
            self._fout.write(line)

    def end(self):
        assert self._state == State.OUT_OF_EDGE

    def _enter_edge(self, src, dst, index):
        assert self._state == State.OUT_OF_EDGE
        self._state = State.IN_EDGE
        self._edge = util.Edge(self._symbol, src, dst, index)

    def _enter_path(self):
        assert self._state == State.IN_EDGE
        self._state = State.IN_PATH
        num_paths = self._paths_per_edge.get(self._edge, 0)
        self._paths_per_edge[self._edge] = num_paths + 1
        fout_base = '%s.%s.xml' % (self._edge.to_file_base(), num_paths)
        self._fout = open(os.path.join(self._out_dir, fout_base), 'w')

    def _exit_path(self):
        assert self._state == State.IN_PATH
        self._state = State.IN_EDGE
        self._fout.close()
        self._fout = None

    def _exit_edge(self):
        assert self._state == State.IN_EDGE
        self._state = State.OUT_OF_EDGE
        self._edge = None

parser = argparse.ArgumentParser()
parser.add_argument('paths_file')
parser.add_argument('out_dir')
args = parser.parse_args()

m = re.match(r'(\w+)\.paths\.xml', os.path.basename(args.paths_file))
fsm = ScannerFSM(m.group(1), args.out_dir)
with open(args.paths_file) as fin:
    for line in fin:
        fsm.process(line)
fsm.end()
