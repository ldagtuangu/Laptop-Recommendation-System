package recommender;

import processor.*;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {

        List<LaptopData> laptops = CsvReader.read("laptop.csv");

        DataCleaner cleaner = new DataCleaner();
        for (LaptopData d : laptops){
            cleaner.clean(d);
        }

        Normalizer normalizer = new Normalizer();
        normalizer.fit(laptops);
        normalizer.normalize(laptops);

        System.out.println("Loaded " + laptops.size() + " laptops.");

        System.out.println("\n═══════════════════════════════════════");

        String[] seeds = {
                "Acer Predator Triton 14 (2025)",            // gaming  → index 0
                "Apple MacBook Pro 14 M5 Pro",   // office  → index 1
                "HP OmniBook 5 AI 16"         // student → index 2
        };

        KMeans kmeans = new KMeans();
        kmeans.fit(laptops, seeds);

        laptops.forEach(d ->
                System.out.println(d.name + " → " + d.category));
    }
}
