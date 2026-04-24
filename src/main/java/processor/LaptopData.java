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
    public double normGpuScore;
    public double normCpuMulti;
    public double normCpuSingle;

    public String category;
    public int clusterId;

    public double[] toVector() {
        return new double[]{
                normBattery,                  // 0
                normWeight,                   // 1
                normScreen,                   // 2
                normResolution,               // 3
                normGpuScore,                 // 4
                normCpuMulti,                 // 5
                normCpuSingle,                // 6
                isAmd   ? 1.0 : 0.0,         // 7
                isIntel ? 1.0 : 0.0          // 8
        };
    }

    @Override
    public String toString() {
        return String.format(
                "[%s] battery=%.1f screen=%.1f weight=%.2f " +
                        "gpu=%s(%.0f) cpuMulti=%.0f cat=%s",
                name, batteryWh, screenInch, weightKg,
                hasDiscreteGpu ? "discrete" : "integrated",
                gpuScore, cpuMulti, category
        );
    }

    public String cpuBrand() {
        if(isApple) return "Apple";
        if (isAmd) return "AMD";
        if (isIntel) return "Intel";
        return "Other";
    }

}
