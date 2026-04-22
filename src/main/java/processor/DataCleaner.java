package processor;

import java.util.List;

public class DataCleaner {

    public LaptopData clean(LaptopData raw){

        raw.batteryWh = parseDouble
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

    public static boolean isDiscrete(String gpu){
        if(gpu == null || gpu.isBlank() || gpu.equals("--")){
            return false;
        }

        String lower = gpu.toLowerCase();

        if (lower.contains("tích hợp"))         return false;
        if (lower.contains("integrated"))        return false;
        if (lower.contains("uhd graphics"))      return false;
        if (lower.contains("iris xe"))           return false;
        if (lower.contains("iris plus"))         return false;
        if (lower.contains("radeon graphics"))   return false;
        if (lower.contains("intel graphics"))    return false;
        if (lower.contains("adreno gpu"))        return false;
        if (lower.contains("apple"))             return false;

        if (lower.contains("rtx"))   return true;
        if (lower.contains("gtx"))   return true;
        if (lower.contains("rx "))   return true;   // AMD RX series
        if (lower.contains("arc b")) return true;   // Intel Arc B series
        if (lower.contains("arc a")) return true;   // Intel Arc A series

        return false;

    }
}
