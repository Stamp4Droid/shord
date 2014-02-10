#!/usr/bin/env python
import sys
import os
import subprocess
import random
import time
import csv

if __name__ == "__main__":
	if len(sys.argv) < 4:
		exit("Need 4 arguments; apps dir; stamp binary; number of samples")

	apps = os.listdir(sys.argv[1])
	num_apps = len(apps)
	print("There are "+str(num_apps)+" apps in the directory...randomly sampling...")

	already_sampled = []
	timings = {}
	w = csv.writer(open("random_samples.csv", "a"))

	for i in range(0,int(sys.argv[3])):
		j = random.randint(1, num_apps)
		while already_sampled.count(j) > 0:
			j = random.randint(1, num_apps)
			
		command = []
		command.append(sys.argv[2])
		command.append('analyze')
		command.append(sys.argv[1]+apps[j])
		print(' '.join(command))
		start_time = time.time()
		exit_code = subprocess.call(command)
		elapsed_time = time.time() - start_time
		timings[apps[j]] = elapsed_time
				
		already_sampled.append(j)
		w.writerow([apps[j], elapsed_time, exit_code])

	

