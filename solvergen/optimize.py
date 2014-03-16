#!/usr/bin/env python
# -*- coding: utf-8 -*-

import argparse
import cfg_parser
import math
import os
import random
import subprocess

def mcmc(b, init, tries):
    curr = init
    curr_cost = cost(curr)
    print 'Original cost: %s' % curr_cost
    best = curr
    best_cost = curr_cost

    counter = 1
    while counter <= tries:
        print 'Trying rewrite #%s' % counter
        counter += 1
        new = curr.clone()
        rand_rewrite(new)
        new_cost = cost(new)
        print '    cost:', new_cost, 'vs', curr_cost
        if new_cost > curr_cost:
            prob = a(b, new_cost, curr_cost)
            print '    probability to pick:', prob
            if random.random() >= prob:
                continue
        print '    picked'
        curr = new
        curr_cost = new_cost
        if curr_cost < best_cost:
            best = curr
            best_cost = curr_cost

    return best

def a(b, new_cost, prev_cost):
    return min(1.0, math.exp(b*(prev_cost-new_cost)))

# TODO: Tune probabilities
def rand_rewrite(grammar):
    mod_kinds = grammar.mod_kinds()
    mods = []
    while len(mods) == 0:
        kind = random.choice(mod_kinds)
        mods = grammar.all_mods(kind)
    (fun, args) = random.choice(mods)
    fun(grammar, *args)

#==============================================================================

class RestartException(Exception):
    pass

def hill_climb(init):
    curr = init
    curr_cost = cost(curr)
    print 'Original cost:', curr_cost

    while True:
        try:
            for kind in curr.mod_kinds():
                for (fun, args) in curr.all_mods(kind):
                    new = curr.clone()
                    fun(new, *args)
                    new_cost = cost(new)
                    if new_cost < curr_cost:
                        curr = new
                        curr_cost = new_cost
                        print 'Rewrite lowered cost to', new_cost
                        print '    %s' % kind
                        for a in args:
                            print '    %s' % a
                        raise RestartException
        except RestartException:
            continue
        break

    return curr

#==============================================================================

parser = argparse.ArgumentParser()
parser.add_argument('cfg_file')
parser.add_argument('test_dir')
args = parser.parse_args()

script_dir = os.path.dirname(os.path.realpath(__file__))
cc_prefix = 'g++-4.8 -std=c++11 -Wall -Wextra -pedantic -O2 -g\
 -DPATH_RECORDING -I "%s"' % script_dir
lib_src = os.path.join(script_dir, 'engine.cpp')
lib_obj = os.path.join(script_dir, 'engine.o')
cpp_file = os.path.join(args.test_dir, 'mcmc_test.cpp')
out_cfg_file = os.path.join(args.test_dir, 'mcmc_test.cfg')
executable = os.path.join(args.test_dir, 'mcmc_test')

def cost(grammar):
    with open(out_cfg_file, 'w') as cfg_out:
        cfg_out.write('%s\n' % grammar)
    with open(cpp_file, 'w') as cpp_out:
        cfg_parser.emit_solver(grammar, cpp_out)
    cc_cmd = cc_prefix + ' -o "%s" "%s" "%s"' % (executable, cpp_file, lib_obj)
    subprocess.check_call(cc_cmd, shell=True)
    run_cmd = 'cd "%s"; ./mcmc_test' % args.test_dir
    num_edges_str = subprocess.check_output(run_cmd, shell=True)
    return int(num_edges_str)

subprocess.check_call(cc_prefix + (' -c "%s"' % lib_src), shell=True)
grammar = cfg_parser.Grammar.from_file(args.cfg_file)
local_optim = hill_climb(grammar)
optim = mcmc(0.005, local_optim, 100)
print
print 'Optimal grammar:'
print optim
