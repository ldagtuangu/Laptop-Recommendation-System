package recommender;

import processor.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws Exception {

        List<LaptopData> all = CsvReader.read("laptop.csv");
        all.removeIf(d -> d.isApple || d.name.contains("Galaxy Tab"));

        System.out.println("After filter: " + all.size() + " laptops");

        DataCleaner cleaner = new DataCleaner();
        for (LaptopData d : all) cleaner.clean(d);

        Normalizer normalizer = new Normalizer();
        normalizer.fit(all);        // fit trên toàn bộ 120 → min/max chính xác
        normalizer.normalize(all);

        System.out.println("Total: " + all.size() + " laptops");
        System.out.println("\n═══════════════════════════════════════");

        List<LaptopData> withBench = new ArrayList<>();
        List<LaptopData> noBench = new ArrayList<>();

        for(LaptopData d : all) {
            if (d.cpuMulti > 0 && d.gpuScore > 0) withBench.add(d);
            else noBench.add(d);
        }

        System.out.println("With benchmark: " + withBench.size());
        System.out.println("No benchmark:   " + noBench.size());
        System.out.println("\n═══════════════════════════════════════");

// K-Means chỉ dùng withBench
        KMeans kmeans = new KMeans();
        kmeans.fitWithIdealCentroids(withBench);

// Sau khi có centroid → predict category cho 48 laptop còn lại
        for (LaptopData d : noBench) {
                d.category = kmeans.predict(d);  // dùng centroid đã train
        }

        List<LaptopData> laptops = new ArrayList<>();
        laptops.addAll(withBench);
        laptops.addAll(noBench);

        Map<String, List<String>> group = new LinkedHashMap<>();
        group.put("gaming", new ArrayList<>());
        group.put("office", new ArrayList<>());
        group.put("creative", new ArrayList<>());

        for (LaptopData d : laptops) {
            group.get(d.category).add(d.name);
        }

        group.forEach((cat, names) -> {
            System.out.println("\n[" + cat.toUpperCase() + "] — " + names.size() + " laptops");
            names.forEach(name -> System.out.println("  • " + name));
        });

        Dataprocessor.saveProcessed(laptops, "laptop_processed.csv");
    }
}
