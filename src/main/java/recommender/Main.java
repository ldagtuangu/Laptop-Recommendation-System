package recommender;

import processor.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws Exception {

        List<LaptopData> all = CsvReader.read("laptop.csv");
        all.removeIf(d -> d.name.contains("Galaxy Tab"));

        DataCleaner cleaner = new DataCleaner();
        for (LaptopData d : all) cleaner.clean(d);

        Normalizer normalizer = new Normalizer();
        normalizer.fit(all);
        normalizer.normalize(all);

        all.removeIf(d -> d.cpuMulti == 0 || d.gpuScore == 0);
        System.out.println("After filter: " + all.size() + " laptops");

        System.out.println("Total: " + all.size() + " laptops");
        System.out.println("\n═══════════════════════════════════════");

        KMeans kmeans = new KMeans();
        kmeans.fit(all);

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

        RecommendationEngine engine = new RecommendationEngine(all);

        UserPreference pref1 = UserPreference.forGaming();
        pref1.cpuBrand = "AMD";
        pref1.topN     = 3;
        RecommendationEngine.printResults(engine.recommend(pref1), pref1);

        UserPreference pref2 = UserPreference.forOffice();
        pref2.topN = 3;
        RecommendationEngine.printResults(engine.recommend(pref2), pref2);

        UserPreference pref3 = UserPreference.forCreative();
        pref3.cpuBrand = "Apple";
        pref3.topN     = 3;
        RecommendationEngine.printResults(engine.recommend(pref3), pref3);

        DataProcessor.saveProcessed(all, "laptop_processed.csv");
    }
}
