package uk.davidwei.perfmock.models.tweeter;

import org.apache.commons.math3.distribution.RealDistribution;
import uk.davidwei.perfmock.internal.perf.distribution.Distribution;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomEmpiricalDistribution implements Distribution {

    private List<Double> distribution = new ArrayList<>();
    private Random rng = new Random();

    public RandomEmpiricalDistribution(String fileName) {
        try {
            InputStreamReader isr = new InputStreamReader(getClass().getResourceAsStream(String.format("/paper/measured/%s", fileName)));
            try (BufferedReader br = new BufferedReader(isr)) {
                String line = br.readLine();
                while (line != null) {
                    distribution.add(Double.parseDouble(line));
                    line = br.readLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public double sample() {
        return distribution.get(rng.nextInt(distribution.size()));
    }

    @Override
    public RealDistribution getDistribution() {
        return null;
    }
}