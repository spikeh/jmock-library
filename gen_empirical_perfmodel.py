import numpy as np
import sys

data = []
with open(sys.argv[1]) as f:
    for line in f:
        val, count = line.split('\t')
        val = float(val)
        count = int(count)
        for i in range(count):
            data.append(val)

data.sort()
hist, bins = np.histogram(data, bins='auto')
mids = bins[:-1] + np.diff(bins) / 2
cdf = np.cumsum(hist)
cdf = cdf / cdf[-1]

class_name = sys.argv[3]
pkg_name = sys.argv[2]

with open(class_name + '.java', 'w') as w:
    w.write('package {};\n'.format(pkg_name));
    w.write('import uk.davidwei.perfmock.internal.perf.distribution.EmpiricalDistribution;\n')
    w.write('public class {} extends EmpiricalDistribution {{\n'.format(class_name))

    w.write('    private static final double[] mids = {')
    for m in mids[:-1]:
        w.write('{:.8f},'.format(m))
    w.write('{:.8f}}};\n'.format(mids[-1]))

    w.write('    private static final double[] cdf = {')
    for m in cdf[:-1]:
        w.write('{:.8f},'.format(m))
    w.write('{:.8f}}};\n'.format(cdf[-1]))

    w.write('    {}() {{\n'.format(class_name));
    w.write('        super(mids, cdf);\n')
    w.write('    }\n')
    w.write('}\n')
