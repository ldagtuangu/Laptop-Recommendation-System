package recommender;

import processor.LaptopData;
import java.util.*;
import java.util.stream.*;

public class RecommendationEngine {

    private final List<LaptopData> allLaptops;

    public RecommendationEngine(List<LaptopData> laptops) {
        this.allLaptops = laptops;
    }

    public List<RecommendResult> recommend(UserPreference pref) {
        pref.normalize();

        List<LaptopData> filtered = hardFilter(allLaptops, pref);
        System.out.printf("After filter: %d / %d laptops%n",
                filtered.size(), allLaptops.size());

        if (filtered.isEmpty()) {
            System.out.println("No laptops match your criteria. Relaxing filters...");
            filtered = new ArrayList<>(allLaptops);
        }

        List<RecommendResult> results = filtered.stream()
                .map(d -> new RecommendResult(
                        d,
                        Scorer.score(d, pref),
                        Scorer.explain(d, pref)
                ))
                .sorted(Comparator.comparingDouble(r -> -r.score))
                .limit(pref.topN)
                .collect(Collectors.toList());

        Set<String> usedNames = results.stream()
                .map(r -> r.laptop.name)
                .collect(Collectors.toSet());
        for (RecommendResult r : results) {
            List<LaptopData> sim = findSimilar(r.laptop, allLaptops, 5);
            sim.removeIf(d -> !usedNames.add(d.name));
            r.similarLaptops = sim.size() > 3 ? sim.subList(0, 3) : sim;
        }

        return results;
    }

    private List<LaptopData> hardFilter(List<LaptopData> laptops, UserPreference pref) {
        return laptops.stream()
                .filter(d -> {
                    // Filter category
                    if (!pref.category.equals("any")
                            && !d.category.equals(pref.category)) return false;

                    // Filter CPU brand
                    if (pref.cpuBrand != null && !pref.cpuBrand.equals("any")) {
                        boolean match = switch (pref.cpuBrand.toLowerCase()) {
                            case "apple" -> d.isApple;
                            case "amd"   -> d.isAmd;
                            case "intel" -> d.isIntel;
                            default      -> true;
                        };
                        if (!match) return false;
                    }

                    // Filter GPU requirement
                    if (pref.wantGpu != null) {
                        if (pref.wantGpu && !d.hasDiscreteGpu) return false;
                        if (!pref.wantGpu && d.hasDiscreteGpu) return false;
                    }

                    return true;
                })
                .collect(Collectors.toList());
    }

    // ── Step 2: Cosine similarity ─────────────────────────────────────────────

    public List<LaptopData> findSimilar(LaptopData target,
                                        List<LaptopData> all,
                                        int n) {
        double[] v = target.toVector();
        return all.stream()
                .filter(d -> !d.name.equals(target.name))
                .sorted(Comparator.comparingDouble(d ->
                        -cosineSimilarity(v, d.toVector())))
                .limit(n)
                .collect(Collectors.toList());
    }

    private double cosineSimilarity(double[] a, double[] b) {
        double dot = 0, magA = 0, magB = 0;
        for (int i = 0; i < a.length; i++) {
            dot  += a[i] * b[i];
            magA += a[i] * a[i];
            magB += b[i] * b[i];
        }
        if (magA == 0 || magB == 0) return 0;
        return dot / (Math.sqrt(magA) * Math.sqrt(magB));
    }

    // ── Print results ─────────────────────────────────────────────────────────

    public static void printResults(List<RecommendResult> results, UserPreference pref) {
        System.out.println("\n══════════════════════════════════════════════════");
        System.out.println(" TOP " + pref.topN + " RECOMMENDATIONS");
        System.out.println(" Category: " + pref.category
                + " | Brand: "   + pref.cpuBrand
                + " | GPU: "     + (pref.wantGpu == null ? "any"
                : pref.wantGpu ? "required" : "not needed"));
        System.out.println("══════════════════════════════════════════════════");

        for (int i = 0; i < results.size(); i++) {
            RecommendResult r = results.get(i);
            System.out.printf("%n#%d %s [%s] — score: %.3f%n",
                    i + 1, r.laptop.name, r.laptop.category, r.score);
            System.out.println("   " + r.explanation);
            System.out.println("   Tags: " + String.join(", ", r.laptop.tags));

            if (!r.similarLaptops.isEmpty()) {
                System.out.print("   Similar: ");
                System.out.println(r.similarLaptops.stream()
                        .map(d -> d.name)
                        .collect(Collectors.joining(" | ")));
            }
        }
        System.out.println("\n══════════════════════════════════════════════════");
    }
}