package recommender;

import processor.LaptopData;
import java.util.*;

public class KMeans {

    private static final int      K        = 3;
    private static final int      MAX_ITER = 100;

    private static final Random RAND = new Random();

    private double[][] centroids = new double[K][];

    public void fit(List<LaptopData> laptops) {
        scaleAppleBenchmark(laptops);
        renormalizeScores(laptops);

        initCentroidsKMeansPlusPlus(laptops);

        System.out.println("K-Means++ k=3 starting...");

        for (int iter = 0; iter < MAX_ITER; iter++) {
            boolean changed = assign(laptops);
            updateCentroids(laptops);
            System.out.printf("  Iter %2d — changed=%b%n", iter + 1, changed);
            if (!changed) {
                System.out.println("  Converged at iter " + (iter + 1));
                break;
            }
        }

        assignLabelsByCentroid(laptops);
    }

    public String predict(LaptopData d) {
        int best = nearestCentroid(d.toVector());
        return switch (best) {
            case 0 -> "gaming";
            case 1 -> "office";
            default -> "creative";
        };
    }

    private void initCentroidsKMeansPlusPlus(List<LaptopData> laptops) {
        LaptopData first = laptops.get(RAND.nextInt(laptops.size()));
        centroids[0] = first.toVector().clone();
        System.out.println("  Centroid 0: " + first.name);

        for (int c = 1; c < K; c++) {
            double[] minDists = new double[laptops.size()];
            double totalDist = 0;

            for (int i = 0; i < laptops.size(); i++) {
                double[] v = laptops.get(i).toVector();
                double minD = Double.MAX_VALUE;
                for (int j = 0; j < c; j++) {
                    double d = euclidean(v, centroids[j]);
                    minD = Math.min(minD, d);
                }
                double d2 = minD * minD;
                minDists[i] = d2;
                totalDist += d2;
            }

            double r = RAND.nextDouble() * totalDist;
            double cum = 0;
            int chosen = 0;
            for (int i = 0; i < laptops.size(); i++) {
                cum += minDists[i];
                if (cum >= r) {
                    chosen = i;
                    break;
                }
            }

            centroids[c] = laptops.get(chosen).toVector().clone();
            System.out.printf("  Centroid %d: %s%n", c, laptops.get(chosen).name);
        }
    }

    private void assignLabelsByCentroid(List<LaptopData> laptops) {
        int gamingIdx = 0, officeIdx = 1, creativeIdx = 2;

        double maxGpu = centroids[0][4];
        gamingIdx = 0;
        for (int i = 1; i < K; i++) {
            if (centroids[i][4] > maxGpu) {
                maxGpu = centroids[i][4];
                gamingIdx = i;
            }
        }

        double maxPort = centroids[0][0] + centroids[0][1];
        officeIdx = 0;
        for (int i = 1; i < K; i++) {
            double p = centroids[i][0] + centroids[i][1];
            if (p > maxPort && i != gamingIdx) {
                maxPort = p;
                officeIdx = i;
            }
        }

        for (int i = 0; i < K; i++) {
            if (i != gamingIdx && i != officeIdx) {
                creativeIdx = i;
                break;
            }
        }

        String[] labels = new String[K];
        labels[gamingIdx] = "gaming";
        labels[officeIdx] = "office";
        labels[creativeIdx] = "creative";

        System.out.printf("  Labels → cluster %d: gaming, %d: office, %d: creative%n",
                gamingIdx, officeIdx, creativeIdx);

        for (LaptopData d : laptops) {
            d.category = labels[d.clusterId];
        }
    }

    private void scaleAppleBenchmark(List<LaptopData> laptops) {
        List<LaptopData> apple   = new ArrayList<>();
        List<LaptopData> windows = new ArrayList<>();
        for (LaptopData d : laptops) {
            if (d.isApple) apple.add(d);
            else           windows.add(d);
        }

        double gpuRatio = safeRatio(
                median(windows, d -> d.gpuScore),
                median(apple,   d -> d.gpuScore));
        double cpuRatio = safeRatio(
                median(windows, d -> d.cpuMulti),
                median(apple,   d -> d.cpuMulti));
        double sRatio   = safeRatio(
                median(windows, d -> d.cpuSingle),
                median(apple,   d -> d.cpuSingle));

        System.out.printf("Apple scale ratios — gpu=%.3f cpuMulti=%.3f cpuSingle=%.3f%n",
                gpuRatio, cpuRatio, sRatio);

        for (LaptopData d : apple) {
            d.gpuScore  *= gpuRatio;
            d.cpuMulti  *= cpuRatio;
            d.cpuSingle *= sRatio;
        }
    }

    private void renormalizeScores(List<LaptopData> laptops) {
        renorm(laptops, d -> d.gpuScore,  (d, v) -> d.normGpuScore  = v);
        renorm(laptops, d -> d.cpuMulti,  (d, v) -> d.normCpuMulti  = v);
        renorm(laptops, d -> d.cpuSingle, (d, v) -> d.normCpuSingle = v);
    }

    private boolean assign(List<LaptopData> laptops) {
        boolean changed = false;
        for (LaptopData d : laptops) {
            int best = nearestCentroid(d.toVector());
            if (best != d.clusterId) {
                d.clusterId = best;
                changed     = true;
            }
        }
        return changed;
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
                System.out.println("  WARNING: cluster " + i + " is empty!");
                continue;
            }
            for (int j = 0; j < vecLen; j++) {
                centroids[i][j] = sums[i][j] / counts[i];
            }
        }
    }

    private int nearestCentroid(double[] v) {
        int    best    = 0;
        double minDist = Double.MAX_VALUE;
        for (int i = 0; i < K; i++) {
            double dist = euclidean(v, centroids[i]);
            if (dist < minDist) {
                minDist = dist;
                best    = i;
            }
        }
        return best;
    }

    private double euclidean(double[] a, double[] b) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            double d = a[i] - b[i];
            sum += d * d;
        }
        return Math.sqrt(sum);
    }

    private double median(List<LaptopData> list, java.util.function.ToDoubleFunction<LaptopData> fn) {
        List<Double> vals = new ArrayList<>();
        for (LaptopData d : list) {
            double v = fn.applyAsDouble(d);
            if (v > 0) vals.add(v);
        }
        if (vals.isEmpty()) return 1.0;
        Collections.sort(vals);
        return vals.get(vals.size() / 2);
    }

    private double safeRatio(double a, double b) {
        return b == 0 ? 1.0 : a / b;
    }

    @FunctionalInterface
    interface Setter { void set(LaptopData d, double v); }

    private void renorm(List<LaptopData> list,
                        java.util.function.ToDoubleFunction<LaptopData> getter,
                        Setter setter) {
        double mn = Double.MAX_VALUE, mx = 0;
        for (LaptopData d : list) {
            double v = getter.applyAsDouble(d);
            if (v > 0) { mn = Math.min(mn, v); mx = Math.max(mx, v); }
        }
        for (LaptopData d : list) {
            double v = getter.applyAsDouble(d);
            if (v <= 0 || mx == mn) setter.set(d, 0.0);
            else setter.set(d, Math.clamp((v - mn) / (mx - mn), 0, 1));
        }
    }
}