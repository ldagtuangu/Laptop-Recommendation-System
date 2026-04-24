package recommender;

import processor.LaptopData;
import java.util.*;

/**
 * K-Means k=2: phân loại laptop chưa được rule-based classify
 * Chỉ phân biệt gaming vs office
 * Creative đã được rule-based xử lý trước
 */
public class KMeans {

    private static final int      K      = 2;
    private static final int      MAX_ITER = 100;
    private static final String[] LABELS = {"gaming", "office"};

    private double[][] centroids = new double[K][];

    //                                     bat   w     scr   res   gpu   amd   intel apple
    private static final double[] CENTROID_GAMING = {0.50, 0.30, 0.70, 0.03, 1.0, 0.40, 0.60, 0.0};
    private static final double[] CENTROID_OFFICE = {0.80, 0.80, 0.40, 0.03, 0.0, 0.40, 0.60, 0.0};

    public void fitWithIdealCentroids(List<LaptopData> laptops) {
        if (laptops.isEmpty()) {
            System.out.println("WARNING: no laptops to cluster.");
            return;
        }

        // Khởi tạo centroid lý tưởng
        centroids[0] = CENTROID_GAMING.clone();
        centroids[1] = CENTROID_OFFICE.clone();

        // Lặp K-Means
        for (int iter = 0; iter < MAX_ITER; iter++) {
            boolean changed = false;

            // Gán mỗi laptop vào centroid gần nhất
            for (LaptopData d : laptops) {
                int newCluster = nearestCentroid(d.toVector());
                if (newCluster != d.clusterId) {
                    d.clusterId = newCluster;
                    changed     = true;
                }
            }

            // Tính lại centroid
            updateCentroids(laptops);

            System.out.printf("  Iter %2d — changed=%b%n", iter + 1, changed);

            // Hội tụ → dừng
            if (!changed) break;
        }

        // Gán category label
        for (LaptopData d : laptops) {
            d.category = LABELS[d.clusterId];
        }
    }

    /**
     * Predict category cho laptop mới dựa vào centroid đã train
     */
    public String predict(LaptopData d) {
        return LABELS[nearestCentroid(d.toVector())];
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private int nearestCentroid(double[] vector) {
        int    best    = 0;
        double minDist = Double.MAX_VALUE;
        for (int i = 0; i < K; i++) {
            double dist = euclidean(vector, centroids[i]);
            if (dist < minDist) {
                minDist = dist;
                best    = i;
            }
        }
        return best;
    }

    private void updateCentroids(List<LaptopData> laptops) {
        int vecLen = centroids[0].length;

        double[][] sums   = new double[K][vecLen];
        int[]      counts = new int[K];

        for (LaptopData d : laptops) {
            int      c = d.clusterId;
            double[] v = d.toVector();
            counts[c]++;
            for (int j = 0; j < vecLen; j++) {
                sums[c][j] += v[j];
            }
        }

        for (int i = 0; i < K; i++) {
            if (counts[i] == 0) {
                System.out.println("WARNING: cluster " + LABELS[i] + " is empty!");
                continue;
            }
            for (int j = 0; j < vecLen; j++) {
                centroids[i][j] = sums[i][j] / counts[i];
            }
        }
    }

    private double euclidean(double[] a, double[] b) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            double diff = a[i] - b[i];
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }

    private String formatVector(double[] v) {
        return String.format(
                "[bat=%.2f w=%.2f scr=%.2f res=%.2f gpu=%.1f amd=%.1f intel=%.1f apple=%.1f]",
                v[0], v[1], v[2], v[3], v[4], v[5], v[6], v[7]);
    }
}