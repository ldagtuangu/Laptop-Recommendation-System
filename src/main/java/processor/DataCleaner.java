package processor;

import java.util.List;

public class DataCleaner {

    public LaptopData clean(LaptopData raw) {
        raw.isApple = isAppleCpu(raw.cpuRaw);
        raw.isAmd   = isAmdCpu(raw.cpuRaw);
        raw.isIntel = isIntelCpu(raw.cpuRaw);

        raw.gpuScore  = parseBenchScore(raw.gpuScoreRaw);
        raw.cpuSingle = parseBenchScore(raw.cpuSingleRaw);
        raw.cpuMulti  = parseBenchScore(raw.cpuMultiRaw);

        raw.hasDiscreteGpu = isDiscrete(raw.gpuScore, raw.gpuRaw);

        return raw;
    }

    public static double parseDouble(String raw, String unit){
        if(raw == null || raw.isBlank() || raw.equals("--"))
            return 0.0;
        String cleaned = raw.replace(unit, "")
                            .replaceAll("[^0-9.]", "")
                            .trim();
        if (cleaned.isEmpty()) return 0.0;
        try{
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e){}
            return 0.0;
    }

    public static double parseWeight(String raw){
        if(raw == null || raw.isBlank() || raw.equals("--"))
            return 0.0;

        boolean isGrams = raw.toLowerCase().contains("g")
                        && !raw.toLowerCase().contains("kg");

        String cleaned = raw.replaceAll("[^0-9.]","").trim();
        if(cleaned.isEmpty()) return 0.0;

        try{
            double val = Double.parseDouble(cleaned);

            if (isGrams || val > 10) val = val / 1000.0;
            if (val < 0.1) val = val * 1000.0;
            return val;
        } catch (NumberFormatException e) {}
            return 0.0;
    }

    public static int[] parseResolution(String raw){
        if (raw == null || raw.isBlank() || raw.equals("--"))
            return new int[]{0, 0};

        String cleaned = raw.replaceAll("[^0-9]", " ").trim();
        String[] parts = cleaned.trim().split("\\s+");
        try{
            if (parts.length >= 2){
                return new int[]{
                        Integer.parseInt(parts[0]),
                        Integer.parseInt(parts[1])
                };
            }
        } catch (NumberFormatException e){}
            return new int[]{0, 0};
    }

    public static double parseBenchScore(String raw){
        if(raw == null || raw.isBlank() || raw.equals("--")){
            return 0.0;
        }
        try{
            return Double.parseDouble(raw.replace(".",""));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public static boolean isDiscrete(double gpuScore, String gpuRaw) {
        if (gpuScore >= 30000) return true;

        if (gpuRaw == null || gpuRaw.isBlank()) return false;
        String l = gpuRaw.toLowerCase();

        // Integrated keywords → false
        if (l.contains("tích hợp"))        return false;
        if (l.contains("integrated"))      return false;
        if (l.contains("uhd"))             return false;
        if (l.contains("iris"))            return false;
        if (l.contains("radeon graphics")) return false;
        if (l.contains("intel graphics"))  return false;
        if (l.contains("adreno gpu"))      return false;
        if (l.contains("apple"))           return false;

        // Discrete keywords → true
        if (l.contains("rtx"))   return true;
        if (l.contains("gtx"))   return true;
        if (l.contains("rx "))   return true;
        if (l.contains("arc b")) return true;  // Arc B390, B580
        if (l.contains("arc a")) return true;  // Arc A770
        if (l.contains("rtx 2000") || l.contains("rtx 3000")
                || l.contains("rtx 4000") || l.contains("rtx 5000")) return true; // Quadro RTX

        return false;
    }

    private boolean isAppleCpu(String cpu) {
        if (cpu == null) return false;
        String l = cpu.toLowerCase();
        return l.contains("apple") || l.contains("m1") || l.contains("m2")
                || l.contains("m3")    || l.contains("m4") || l.contains("m5")
                || l.contains("a18");
    }

    private boolean isAmdCpu(String cpu) {
        if (cpu == null) return false;
        return cpu.toLowerCase().contains("amd")
                || cpu.toLowerCase().contains("ryzen");
    }

    private boolean isIntelCpu(String cpu) {
        if (cpu == null) return false;
        return cpu.toLowerCase().contains("intel")
                || cpu.toLowerCase().contains("core");
    }
}
