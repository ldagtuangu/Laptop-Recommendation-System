package processor;

public class LaptopData {

    public String name;
    public String cpuRaw;
    public String gpuRaw;
    public String link;

    public String gpuScoreRaw;
    public String cpuSingleRaw;
    public String cpuMultiRaw;

    public double gpuScore;
    public double cpuSingle;
    public double cpuMulti;

    public double batteryWh;
    public double weightKg;
    public double screenInch;
    public int resolutionW;
    public int resolutionH;
    public long totalPixels;

    public boolean hasDiscreteGpu;
    public boolean isAmd;
    public boolean isIntel;
    public boolean isApple;

    public double normBattery;
    public double normWeight;
    public double normScreen;
    public double normResolution;

    public String category;

    @Override
    public String toString() {
        return String.format(
                "[%s] battery=%.1f screen=%.1f weight=%.2f gpu=%s cpu=%s cat=%s",
                name, batteryWh, screenInch, weightKg,
                hasDiscreteGpu ? "discrete" : "integrated",
                cpuBrand(), category
        );
    }

    public String cpuBrand() {
        if(isApple) return "Apple";
        if (isAmd) return "AMD";
        if (isIntel) return "Intel";
        return "Other";
    }

}
