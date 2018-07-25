package org.jmock.internal.perf.distribution;

import org.apache.commons.math3.distribution.RealDistribution;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Alias implements Distribution, Serializable {
    private static final long serialVersionUID = -2218446065547666620L;
    private int[] alias;
    private double[] probability;
    private Random random;
    private List<Double> vals = new ArrayList<>();

    public Alias(String filePath) throws IOException {
        Path p = Paths.get(filePath);
        Charset cs = Charset.forName("ISO-8859-1");
        List<String> lines = Files.readAllLines(p, cs);
        List<Double> probabilities = setProbabilities(lines);
        load(probabilities);
    }

    public Alias(BufferedReader br) throws IOException {
        List<String> lines = new ArrayList<>();
        String line = br.readLine();
        while (line != null) {
            lines.add(line);
            line = br.readLine();
        }
        List<Double> probabilities = setProbabilities(lines);
        load(probabilities);
    }

    private List<Double> setProbabilities(List<String> lines) {
        int count = 0;
        List<Double> probabilities = new ArrayList<>();
        for (String l : lines) {
            String[] split = l.split("\t");
            vals.add(Double.parseDouble(split[0]));
            count += Integer.parseInt(split[1]);
        }
        for (String l : lines) {
            String[] split = l.split("\t");
            probabilities.add(Double.parseDouble(split[1]) / count);
        }
        return probabilities;
    }

    private void load(List<Double> probabilities) {
        alias = new int[probabilities.size()];
        probability = new double[probabilities.size()];
        random = new Random();

        final double average = 1.0 / probabilities.size();

        probabilities = new ArrayList<Double>(probabilities);

        Deque<Integer> small = new ArrayDeque<Integer>();
        Deque<Integer> large = new ArrayDeque<Integer>();

        for (int i = 0; i < probabilities.size(); ++i) {
            if (probabilities.get(i) >= average) {
                large.add(i);
            } else {
                small.add(i);
            }
        }

        while (!small.isEmpty() && !large.isEmpty()) {
            int less = small.removeLast();
            int more = large.removeLast();

            probability[less] = probabilities.get(less) * probabilities.size();
            alias[less] = more;

            probabilities.set(more, (probabilities.get(more) + probabilities.get(less)) - average);

            if (probabilities.get(more) >= 1.0 / probabilities.size()) {
                large.add(more);
            } else {
                small.add(more);
            }
        }

        while (!small.isEmpty()) {
            probability[small.removeLast()] = 1.0;
        }

        while (!large.isEmpty()) {
            probability[large.removeLast()] = 1.0;
        }
    }

    @Override
    public double sample() {
        int column = random.nextInt(probability.length);

        boolean toss = random.nextDouble() < probability[column];

        int i = toss ? column : alias[column];

        return vals.get(i);
    }

    @Override
    public RealDistribution getDistribution() {
        return null;
    }
}
