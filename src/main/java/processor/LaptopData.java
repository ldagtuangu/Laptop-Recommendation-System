package processor;

import java.util.ArrayList;
import java.util.List;

public class LaptopData {

    public String resolution;
    public String screenSize;
    public String weight;
    public String battery;
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

    public LaptopData(){}

    public LaptopData(String name, String cpu, String gpu, String battery,
                  String weight, String screenSize, String resolution, String cpuSingleScore ,String cpuMultiScore, String gpuScore, String link){
        this.name = name;
        this.cpuRaw = cpu;
        this.gpuRaw = gpu;
        this.battery = battery;
        this.weight = weight;
        this.screenSize = screenSize;
        this.resolution = resolution;
        this.cpuSingleRaw = cpuSingleScore;
        this.cpuMultiRaw = cpuMultiScore;
        this.gpuScoreRaw = gpuScore;
        this.link = link;
    }

    public List<String> tags = new ArrayList<>();

    public double[] toVector() {
        return new double[]{
                normBattery,
                normWeight,
                normScreen,
                normResolution,
                normGpuScore,
                normCpuMulti,
                normCpuSingle,// 3
                isGamingGpu ? 1.0 : 0.0
        };
    }

    public String toCSV() {
        return name + "," + cpuRaw + "," + gpuRaw + "," + battery + "," +
                weight + "," + screenSize + "," + resolution + "," + cpuSingleRaw + "," + cpuMultiRaw + "," + gpuScoreRaw + "," + link;
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
