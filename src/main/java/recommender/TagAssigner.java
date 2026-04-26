package recommender;

import processor.LaptopData;
import java.util.*;

public class TagAssigner {

    public static List<String> assignTags(LaptopData d) {
        List<String> tags = new ArrayList<>();

        // ── GPU performance ──────────────────────────────────────────────────
        if (d.normGpuScore      >= 0.55) tags.add("high GPU");
        else if (d.normGpuScore >= 0.43) tags.add("mid GPU");
        else if (d.normGpuScore >  0.0)  tags.add("low GPU");

        // ── CPU performance ──────────────────────────────────────────────────
        if (d.normCpuMulti      >= 0.44) tags.add("high CPU");
        else if (d.normCpuMulti >= 0.10) tags.add("mid CPU");
        else if (d.normCpuMulti >  0.0)  tags.add("low CPU");

        if (d.normCpuSingle     >= 0.47) tags.add("fast single core");

        // ── Display ──────────────────────────────────────────────────────────
        if (d.normResolution    >= 0.55) tags.add("4K+ display");
        else if (d.normResolution >= 0.44) tags.add("3K display");
        else if (d.normResolution >= 0.28) tags.add("2K display");
        else                             tags.add("FHD display");

        if (d.normScreen        >= 0.58) tags.add("large screen");       // >= 16 inch
        else if (d.normScreen   >= 0.15) tags.add("mid screen");         // 14-15 inch
        else                            tags.add("compact screen");       // <= 13 inch

        // ── Battery ──────────────────────────────────────────────────────────
        if (d.normBattery       >= 0.83) tags.add("excellent battery");  // top 20%
        else if (d.normBattery  >= 0.59) tags.add("good battery");       // p66-p80
        else if (d.normBattery  >= 0.42) tags.add("average battery");    // p33-p66
        else                            tags.add("weak battery");

        // ── Weight ───────────────────────────────────────────────────────────
        if (d.normWeight        >= 0.89) tags.add("ultralight");         // top 20%
        else if (d.normWeight   >= 0.69) tags.add("lightweight");        // p33-p80
        else if (d.normWeight   >= 0.50) tags.add("medium weight");
        else                            tags.add("heavy");

        // ── GPU type ─────────────────────────────────────────────────────────
        if (d.isGamingGpu)                                  tags.add("gaming GPU");
        else if (isCreativeGpu(d.gpuRaw)) tags.add("workstation GPU");
        else if (d.hasDiscreteGpu)                          tags.add("discrete GPU");
        else                                                tags.add("integrated GPU");

        // ── CPU brand ────────────────────────────────────────────────────────
        if (d.isApple)  tags.add("Apple Silicon");
        if (d.isAmd)    tags.add("AMD");
        if (d.isIntel)  tags.add("Intel");

        // ── Category-specific ────────────────────────────────────────────────
        switch (d.category) {
            case "gaming" -> {
                if (d.normGpuScore >= 0.55) tags.add("high-end gaming");
                else                        tags.add("mid-range gaming");
            }
            case "creative" -> {
                if (d.normResolution >= 0.55) tags.add("pro display");
                if (d.normCpuMulti   >= 0.44) tags.add("fast rendering");
                if (d.isApple)                tags.add("optimized ecosystem");
            }
            case "office" -> {
                if (d.normBattery  >= 0.59
                        && d.normWeight   >= 0.69) tags.add("highly portable");
                if (d.normCpuSingle >= 0.47) tags.add("responsive");
            }
        }

        return tags;
    }

    private static boolean isCreativeGpu(String gpuRaw) {
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