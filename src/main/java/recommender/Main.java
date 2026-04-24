package recommender;

import processor.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) throws Exception {

        List<LaptopData> all = CsvReader.read("laptop.csv");
        all.removeIf(d -> d.name.contains("Galaxy Tab"));
        all.removeIf(d -> d.cpuMulti == 0 || d.gpuScore == 0);
        System.out.println("After filter: " + all.size() + " laptops");

        DataCleaner cleaner = new DataCleaner();
        for (LaptopData d : all) cleaner.clean(d);

        Normalizer normalizer = new Normalizer();
        normalizer.fit(all);
        normalizer.normalize(all);

        System.out.println("Total: " + all.size() + " laptops");
        System.out.println("\n═══════════════════════════════════════");

        for (LaptopData d : all){
            String lower = d.name.toLowerCase();

            if (LaptopClassifier.isGamingName(lower)
                || (d.isGamingGpu && d.normWeight <= 0.5)) {
                d.category = "gaming";
                continue;
            }

            if (d.isApple
                    || LaptopClassifier.isCreativeGpu(d.gpuRaw)
                    || d.normResolution >= 0.55) {
                d.category = "creative";
            }
        }

        long gaming   = all.stream().filter(d -> "gaming".equals(d.category)).count();
        long creative = all.stream().filter(d -> "creative".equals(d.category)).count();
        long unclassifiedCount = all.stream().filter(d -> d.category == null).count();
        System.out.println("Rule-based → gaming:   " + gaming);
        System.out.println("Rule-based → creative: " + creative);
        System.out.println("Unclassified:          " + unclassifiedCount);


        List<LaptopData> unclassified = all.stream()
                .filter(d -> d.category == null)
                .collect(Collectors.toList());

        KMeans kmeans = new KMeans();
        kmeans.fitWithIdealCentroids(unclassified);

        Map<String, List<String>> group = new LinkedHashMap<>();
        group.put("gaming", new ArrayList<>());
        group.put("office", new ArrayList<>());
        group.put("creative", new ArrayList<>());

        for (LaptopData d : all) {
            group.get(d.category).add(d.name);
        }

        group.forEach((cat, names) -> {
            System.out.println("\n[" + cat.toUpperCase() + "] — " + names.size() + " laptops");
            names.forEach(name -> System.out.println("  • " + name));
        });

        for (LaptopData d : all) {
            d.tags = TagAssigner.assignTags(d);
        }

        Dataprocessor.saveProcessed(all, "laptop_processed.csv");
    }

    private static double euclidean(double[] a, double[] b) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            double diff = a[i] - b[i];
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }
}
