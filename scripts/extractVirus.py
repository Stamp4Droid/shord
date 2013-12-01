# extract app related to my 18 families in virusshare.
import xml.etree.cElementTree as ET
import os
import sys
from subprocess import PIPE, Popen
import fileinput
import datetime
import shutil
import signal
import time
from signal import alarm, signal, SIGALRM, SIGKILL, SIGTERM
from random import randint
import re

malwareSet = ['geinimi']

for family in malwareSet:
    #create folder
    os.makedirs(family)

    for line in open("virusReport-v4.txt"):
        if ('Full report' in line) and (re.search(family, line, re.IGNORECASE)):
            #copy file to folder
            apkcode = line[0:32]


