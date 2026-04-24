package recommender;

import processor.LaptopData;

public class LaptopClassifier {

    public static String classify(LaptopData d) {
        String lower = d.name.toLowerCase();
        if (isGamingName(lower))                  return "gaming";
        if (d.isGamingGpu && d.normWeight <= 0.5) return "gaming";

        if (d.isApple)                            return "creative";
        if (isCreativeGpu(d.gpuRaw))              return "creative";
        if (d.normResolution >= 0.55)             return "creative";

        if (lower.contains("zenbook duo")) return "office";
        return "office";
    }

    public static boolean isGamingName(String lower) {
        return lower.contains("rog")
                || lower.contains("tuf gaming")
                || lower.contains("tuf a")
                || lower.contains("tuf f")
                || lower.contains("predator")
                || lower.contains("nitro")
                || lower.contains("legion")
                || lower.contains("omen")
                || lower.contains("strix")
                || lower.contains("zephyrus")
                || lower.contains("katana")
                || lower.contains("cyborg")
                || lower.contains("stealth")
                || lower.contains("helios")
                || lower.contains("aorus")
                || lower.contains("triton")
                || lower.contains("victus")
                || lower.contains("gaming a")
                || lower.contains("gaming v")
                || lower.contains("gaming aero");
    }

    public static boolean isCreativeGpu(String gpuRaw) {
        if (gpuRaw == null || gpuRaw.isBlank()) return false;
        String l = gpuRaw.toLowerCase();
        return l.contains("quadro")
                || l.contains("rtx 2000 ada")
                || l.contains("rtx 3000 ada")
                || l.contains("rtx 4000 ada")
                || l.contains("rtx 5000 ada")
                || l.contains("rtx pro")
                || l.contains("arc pro")
                || l.contains("arc b");
    }
}