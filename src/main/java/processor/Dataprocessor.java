package processor;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Dataprocessor {

    public static void main(String[] args) throws Exception {
        String inputPath = "laptop.csv";
        String outputPath = "laptop_processed.csv";

        List<LaptopData> laptops = CsvReader.read(inputPath);
        if (laptops.isEmpty()) {
            return;
        }

        DataCleaner cleaner = new DataCleaner();
        for(LaptopData d : laptops) {
            cleaner.clean(d);
        }
        System.out.println("Done.");

        printMissingReport(laptops);

        Normalizer normalizer = new Normalizer();
        normalizer.fit(laptops);
        normalizer.normalize(laptops);
        System.out.println("Normalization done");

        printCategoryStats(laptops);

        saveProcessed(laptops, outputPath);

        System.out.println("\n── Sample rows ──────────────────────────────────");
        laptops.stream().limit(5).forEach(System.out::println);

    }

    public static void saveProcessed(List<LaptopData> laptops, String path) throws IOException {
        try(BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(path), StandardCharsets.UTF_8))){

            for (LaptopData d : laptops) {
                bw.write(String.join(",",
                        escape(d.name),
                        escape(d.cpuRaw),
                        escape(d.gpuRaw),
                        fmt(d.batteryWh),
                        fmt(d.weightKg),
                        fmt(d.screenInch),
                        String.valueOf(d.resolutionW),
                        String.valueOf(d.resolutionH),
                        String.valueOf(d.totalPixels),
                        fmt(d.gpuScore),
                        fmt(d.cpuSingle),
                        fmt(d.cpuMulti),
                        bool(d.hasDiscreteGpu),
                        bool(d.isAmd),
                        bool(d.isIntel),
                        bool(d.isApple),
                        fmt4(d.normBattery),
                        fmt4(d.normWeight),
                        fmt4(d.normScreen),
                        fmt4(d.normResolution),
                        fmt4(d.normGpuScore),
                        fmt4(d.normCpuMulti),
                        fmt4(d.normCpuSingle),
                        d.category,
                        String.join(", ", d.tags),
                        d.link
                ));
                bw.newLine();
            }
        }
        System.out.println("Saved -> " + path);
    }

    private static void printMissingReport(List<LaptopData> laptops) {
        int missBattery = 0, missWeight = 0, missScreen = 0;
        int missRes = 0, missGpu = 0, missCpu = 0;

        for(LaptopData d : laptops) {
            if (d.batteryWh   == 0) missBattery++;
            if (d.weightKg    == 0) missWeight++;
            if (d.screenInch  == 0) missScreen++;
            if (d.totalPixels == 0) missRes++;
            if (d.gpuScore    == 0) missGpu++;
            if (d.cpuMulti    == 0) missCpu++;
        }
        int n = laptops.size();
        System.out.println("\n── Missing value report ─────────────────────────");
        System.out.printf("  battery:    %d / %d%n", missBattery, n);
        System.out.printf("  weight:     %d / %d%n", missWeight,  n);
        System.out.printf("  screen:     %d / %d%n", missScreen,  n);
        System.out.printf("  resolution: %d / %d%n", missRes,     n);
        System.out.printf("  gpuScore:   %d / %d%n", missGpu,     n);
        System.out.printf("  cpuMulti:   %d / %d%n", missCpu,     n);
    }

    private static void printCategoryStats(List<LaptopData> laptops){
        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("gaming", 0);
        counts.put("student", 0);
        counts.put("office", 0);
        for (LaptopData d : laptops){
            counts.merge(d.category, 1, Integer::sum);
        }
        System.out.println("\n Category distribution");
        counts.forEach((cat, cnt) ->
                System.out.printf(" %-8s: %d%n", cat, cnt));
    }

    private static String fmt(double v) {
        return String.format("%.3f", v);
    }

    private static String fmt4(double v) {
        return String.format("%.4f", v);
    }

    private static String bool(boolean b){
        return b ? "1" : "0";
    }

    private static String escape(String s) {
        if (s == null) return "";
        if (s.contains(",")) return "\"" + s + "\"";
        return s;
    }
}
