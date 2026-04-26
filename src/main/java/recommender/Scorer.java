package recommender;

import processor.LaptopData;
import java.util.*;

public class Scorer {

    public static double score(LaptopData d, UserPreference pref) {
        double performance = (d.normGpuScore * 0.5)
                + (d.normCpuMulti * 0.35)
                + (d.normCpuSingle * 0.15);

        double portability = (d.normBattery * 0.5)
                + (d.normWeight * 0.5);

        double display = (d.normResolution * 0.6)
                + (d.normScreen * 0.4);

        double score = (pref.weightPerformance * performance)
                + (pref.weightPortability * portability)
                + (pref.weightDisplay * display);

        if (!pref.category.equals("any") && !pref.cpuBrand.equals("any")) {
            boolean match = switch (pref.cpuBrand.toLowerCase()) {
                case "apple" -> d.isApple;
                case "amd" -> d.isAmd;
                case "intel" -> d.isIntel;
                default -> false;
            };

            if (match) score += 0.03;
        }

        return Math.clamp(score, 0.0, 1.0);
    }

    public static String explain(LaptopData d, UserPreference pref) {
        List<String> reasons = new ArrayList<>();

        // Performance reasons
        if (pref.weightPerformance >= 0.3) {
            if (d.normGpuScore  >= 0.55) reasons.add("high GPU performance");
            if (d.normCpuMulti  >= 0.44) reasons.add("strong multi-core CPU");
            if (d.normCpuSingle >= 0.47) reasons.add("fast single-core CPU");
        }

        // Portability reasons
        if (pref.weightPortability >= 0.3) {
            if (d.normBattery >= 0.59) reasons.add("good battery life");
            if (d.normWeight  >= 0.69) reasons.add("lightweight");
        }

        // Display reasons
        if (pref.weightDisplay >= 0.3) {
            if (d.normResolution >= 0.44) reasons.add("high resolution display");
            if (d.normScreen     >= 0.58) reasons.add("large screen");
        }

        // Category match
        if (!pref.category.equals("any") && d.category.equals(pref.category)) {
            reasons.add("matches your use case (" + pref.category + ")");
        }

        // Brand match
        if (pref.cpuBrand != null && !pref.cpuBrand.equals("any")) {
            boolean match = switch (pref.cpuBrand.toLowerCase()) {
                case "apple" -> d.isApple;
                case "amd"   -> d.isAmd;
                case "intel" -> d.isIntel;
                default      -> false;
            };
            if (match) reasons.add("preferred CPU brand (" + pref.cpuBrand + ")");
        }

        if (reasons.isEmpty()) reasons.add("best overall match");

        return "Recommended because of: " + String.join(", ", reasons);
    }
}
