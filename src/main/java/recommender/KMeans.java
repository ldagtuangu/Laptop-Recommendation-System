package recommender;

import processor.LaptopData;
import java.util.*;

public class KMeans {

    private static final int K = 3;
    private static final int MAX_ITER = 100;

    private static final String[] LABELS = {"gaming", "office", "creative"};
    private double[][] centroids = new double[K][];

    //                                    bat   w     scr   res   gpu   cpuM  cpuS  amd   intel
    private static final double[] CENTROID_GAMING   = {0.30, 0.30, 0.50, 0.50, 0.90, 0.50, 0.75, 0.33, 0.67};
    private static final double[] CENTROID_OFFICE   = {0.90, 0.90, 0.50, 0.40, 0.10, 0.35, 0.50, 0.33, 0.67};
    private static final double[] CENTROID_CREATIVE = {0.50, 0.50, 0.90, 0.75, 0.70, 0.90, 0.50, 0.33, 0.67};

    public void fitWithIdealCentroids(List<LaptopData> laptops) {
        // Khởi tạo centroid lý tưởng
        centroids[0] = CENTROID_GAMING;
        centroids[1] = CENTROID_OFFICE;
        centroids[2] = CENTROID_CREATIVE;

        System.out.println("Using ideal centroids:");
        System.out.println("  [gaming]   " + formatVector(centroids[0]));
        System.out.println("  [office]   " + formatVector(centroids[1]));
        System.out.println("  [creative] " + formatVector(centroids[2]));

        // Chạy K-Means bình thường
        for (int iter = 0; iter < MAX_ITER; iter++) {
            boolean changed = false;

            for (LaptopData d : laptops) {
                int newCluster = nearestCentroid(d.toVector());
                if (newCluster != d.clusterId) {
                    d.clusterId = newCluster;
                    changed     = true;
                }
            }

            updateCentroids(laptops);
            System.out.printf("Iter %2d — changed=%b%n", iter + 1, changed);

            if (!changed) break;
        }

        // Gán category label
        for (LaptopData d : laptops) {
            d.category = LABELS[d.clusterId];
        }
    }

    private String formatVector(double[] v) {
        return String.format(
                "[bat=%.2f w=%.2f scr=%.2f res=%.2f gpu=%.2f cpuM=%.2f cpuS=%.2f]",
                v[0], v[1], v[2], v[3], v[4], v[5], v[6]);
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

    private void updateCentroids(List<LaptopData> laptops){
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

    public String predict(LaptopData d) {
        int cluster = nearestCentroid(d.toVector());
        return LABELS[cluster];
    }

}
