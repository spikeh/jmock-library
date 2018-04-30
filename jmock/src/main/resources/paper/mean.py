#!/usr/bin/env python
import sys

total = 0.0
count = 0
with open(sys.argv[1]) as f:
    for line in f:
        val, nr = line.rstrip().split('\t')
        total += float(val) * int(nr)
        count += float(nr)
print(total / count)
