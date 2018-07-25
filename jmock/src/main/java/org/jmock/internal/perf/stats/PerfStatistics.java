package org.jmock.internal.perf.stats;

import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jmock.internal.perf.distribution.Distribution;

import static org.hamcrest.number.OrderingComparison.greaterThanOrEqualTo;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class PerfStatistics {
    private static final double[] EMPTY_DOUBLE_ARRAY = new double[0];

    public static Matcher<List<Double>> hasPercentile(final int i, final Matcher<Double> percentileCheck) {
        return new TypeSafeMatcher<List<Double>>() {
            @Override
            protected boolean matchesSafely(List<Double> doubles) {
                System.out.println("Percentile: " + percentile(i, doubles));
                return percentileCheck.matches(percentile(i, doubles));
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("percentile " + i + " to be " + percentileCheck);
            }

            @Override
            protected void describeMismatchSafely(List<Double> doubles, Description description) {
                description.appendValue(percentile(i, doubles));
            }
        };
    }

    public static Matcher<List<Double>> hasMean(final Matcher<Double> meanCheck) {
        return new TypeSafeMatcher<List<Double>>() {
            @Override
            protected boolean matchesSafely(List<Double> doubles) {
                return meanCheck.matches(mean(doubles));
            }

            @Override
            public void describeTo(Description description) {
                description.appendValue(meanCheck);
            }

            @Override
            protected void describeMismatchSafely(List<Double> doubles, Description description) {
                description.appendValue(mean(doubles));
            }
        };
    }

    public static Matcher<List<Double>> hasMedian(final Matcher<Double> medianCheck) {
        return new TypeSafeMatcher<List<Double>>() {
            @Override
            protected boolean matchesSafely(List<Double> doubles) {
                return medianCheck.matches(median(doubles));
            }

            @Override
            public void describeTo(Description description) {
                description.appendValue(medianCheck);
            }

            @Override
            protected void describeMismatchSafely(List<Double> doubles, Description description) {
                description.appendValue(median(doubles));
            }
        };
    }

    public static Matcher<List<Double>> matchDistribution(Distribution distribution) {
        return new TypeSafeMatcher<List<Double>>() {
            double pvalue;
            String mismatch;

            @Override
            protected boolean matchesSafely(List<Double> doubles) {
                /*
                double[] data = toPrimitive(doubles);
                pvalue = new KolmogorovSmirnovTest().kolmogorovSmirnovTest(distribution, data);
                return greaterThanOrEqualTo(0.01).matches(pvalue);
                */

                RealDistribution underlyingDist = distribution.getDistribution();
                if (underlyingDist == null)
                    return false;

                double sampleMean = mean(doubles);
                double matchMean = underlyingDist.getNumericalMean();
                if (sampleMean > matchMean) {
                    mismatch = String.format("Sample mean: %f does not match distribution mean: %f", sampleMean, matchMean);
                    return false;
                }

                Percentile percentile = new Percentile();
                double[] samples = toPrimitive(doubles);
                double[] compare = new double[10000];
                for (int i = 0; i < 10000; i++) {
                    compare[i] = distribution.sample();
                }
                for (int i = 10; i <= 80; i += 10) {
                    double samplesPercentile = percentile.evaluate(samples, i);
                    double comparePercentile = percentile.evaluate(compare, i);
                    if (samplesPercentile > comparePercentile) {
                        mismatch = String.format("%dth percentile doesn't match", i);
                        return false;
                    }
                }
                return true;
            }

            @Override
            public void describeTo(Description description) {
                description.appendValue("percentile should be smaller");
            }

            @Override
            protected void describeMismatchSafely(List<Double> doubles, Description description) {
                description.appendValue(mismatch);
            }
        };
    }

    private static double percentile(int i, List<Double> runTimes) {
        return new Percentile().evaluate(toPrimitive(runTimes), i);
    }

    private static double mean(List<Double> runTimes) {
        Double sum = 0.0;
        for (Double d : runTimes) {
            sum += d;
        }
        return sum / runTimes.size();
    }

    private static double median(List<Double> runTimes) {
        Collections.sort(runTimes);
        if (runTimes.size() % 2 == 0) {
            int mid = runTimes.size() / 2;
            return (runTimes.get(mid) + runTimes.get(mid - 1)) / 2;
        } else {
            return runTimes.get(runTimes.size() / 2);
        }
    }

    private static double[] toPrimitive(final List<Double> array) {
        if (array == null)
            return null;
        else if (array.size() == 0)
            return EMPTY_DOUBLE_ARRAY;
        final double[] result = new double[array.size()];
        Iterator<Double> it = array.iterator();
        for (int i = 0; i < result.length; i++) {
            result[i] = it.next();
        }
        return result;
    }
}