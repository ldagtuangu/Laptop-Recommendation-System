package processor;

import java.util.ArrayList;
import java.util.List;

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
    public boolean isGamingGpu;

    public double normBattery;
    public double normWeight;
    public double normScreen;
    public double normResolution;
    public double normGpuScore;
    public double normCpuMulti;
    public double normCpuSingle;

    public String category;
    public int clusterId;

    public List<String> tags = new ArrayList<>();

    public double[] toVector() {
        return new double[]{
                normBattery,                    // 0
                normWeight,                     // 1
                normScreen,                     // 2
                normResolution,
                normGpuScore,
                normCpuMulti,
                normCpuSingle,// 3
                isGamingGpu ? 1.0 : 0.0      // 4 ← isGamingGpu thay hasDiscreteGpu
         // 7
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
