package recommender;

import processor.LaptopData;
import java.util.*;

public class KMeans {

    private static final int K = 3;
    private static final int MAX_ITER = 100;

    private static final String[] LABELS = {"gaming", "office", "student"};
    private double[][] centroids = new double[K][];

    public void fit(List<LaptopData> laptops, String[] seedNames){
        for(int i = 0; i < K; i++){
            final String name = seedNames[i];
            LaptopData seed = laptops.stream()
                    .filter(d -> d.name.equalsIgnoreCase(name))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Seed not found: " + name));

            centroids[i] = seed.toVector();
            System.out.println("Seed [" + LABELS[i] + "] → " + seed.name);
        }
        for (int iter = 0; iter < MAX_ITER; iter++){
            boolean changed = false;
            for(LaptopData d : laptops) {
                int newCluster = nearestCentroid(d.toVector());
                if (newCluster != d.clusterId) {
                    d.clusterId = newCluster;
                    changed = true;
                }
            }

            updateCentroid(laptops);
            System.out.printf("Iter %2d — changed=%b%n", iter + 1, changed);

            if(!changed) break;

        }

        for (LaptopData d : laptops) {
            d.category = LABELS[d.clusterId];
        }
    }

    private int nearestCentroid(double[] vector){
        int best = 0;
        double minDist = Double.MAX_VALUE;

        for (int i = 0; i < K; i++){
            double dist = euclidean(vector, centroids[i]);
            if(dist < minDist){
                minDist = dist;
                best = i;
            }
        }
        return best;
    }

    private void updateCentroid(List<LaptopData> laptops){
        int vecLen = centroids[0].length;
        double[][] sums = new double[K][vecLen];
        int[] counts = new int[K];

        for (LaptopData d : laptops) {
            double[] v = d.toVector();
            int c = d.clusterId;
            counts[c]++;
            for (int j = 0; j < vecLen; j++){
                sums[c][j] += v[j];
            }
        }

        for (int i = 0; i < K; i++){
            if (counts[i] == 0) continue;
            for (int j = 0; j < vecLen; j++){
                centroids[i][j] = sums[i][j]/counts[i];
            }
        }
    }

    private double euclidean(double[] a, double[] b) {
        double sum = 0;
        for (int i = 0; i < a.length; i++){
            double diff = a[i] - b[i];
            sum += diff*diff;
        }
        return Math.sqrt(sum);
    }

}
