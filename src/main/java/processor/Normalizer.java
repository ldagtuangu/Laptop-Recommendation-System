package processor;

import java.util.*;

public class Normalizer {

    private double minBattery, maxBattery;
    private double minWeight, maxWeight;
    private double minScreen, maxScreen;
    private double minPixels, maxPixels;
    private double minGpuScore, maxGpuScore;
    private double minCpuMulti, maxCpuMulti;
    private double minCpuSingle, maxCpuSingle;

    public void fit(List<LaptopData> laptops) {
        minBattery = minWeight = minScreen = Double.MAX_VALUE;
        minPixels = minGpuScore = minCpuMulti = Double.MAX_VALUE;
        minCpuSingle = Double.MAX_VALUE;

        maxBattery = maxWeight = maxScreen = 0;
        maxPixels = maxGpuScore = maxCpuMulti = 0;
        maxCpuSingle = 0;

        for (LaptopData d : laptops) {
            if (d.batteryWh > 0) {
                minBattery = Math.min(minBattery, d.batteryWh);
                maxBattery = Math.max(maxBattery, d.batteryWh);
            }
            if (d.weightKg > 0) {
                minWeight = Math.min(minWeight, d.weightKg);
                maxWeight = Math.max(maxWeight, d.weightKg);
            }
            if (d.screenInch > 0) {
                minScreen = Math.min(minScreen, d.screenInch);
                maxScreen = Math.max(maxScreen, d.screenInch);
            }
            if (d.totalPixels > 0) {
                minPixels = Math.min(minPixels, d.totalPixels);
                maxPixels = Math.max(maxPixels, d.totalPixels);
            }
            if (d.gpuScore > 0) {
                minGpuScore = Math.min(minGpuScore, d.gpuScore);
                maxGpuScore = Math.max(maxGpuScore, d.gpuScore);
            }
            if (d.cpuMulti > 0) {
                minCpuMulti = Math.min(minCpuMulti, d.cpuMulti);
                maxCpuMulti = Math.max(maxCpuMulti, d.cpuMulti);
            }
            if (d.cpuSingle > 0) {
                minCpuSingle = Math.min(minCpuSingle, d.cpuSingle);
                maxCpuSingle = Math.max(maxCpuSingle, d.cpuSingle);
            }

        }

        printStats();
    }

    public void normalize(List<LaptopData> laptops){
        for(LaptopData d : laptops) {
            d.normBattery = minMax(d.batteryWh, minBattery, maxBattery, false);
            d.normWeight = minMax(d.weightKg, minWeight, maxWeight, true);
            d.normScreen = minMax(d.screenInch, minScreen, maxScreen, false);
            d.normResolution = minMax(d.totalPixels, minPixels, maxPixels, false);
            d.normGpuScore  = minMax(d.gpuScore,     minGpuScore,  maxGpuScore,  false);
            d.normCpuMulti  = minMax(d.cpuMulti,     minCpuMulti,  maxCpuMulti,  false);
            d.normCpuSingle = minMax(d.cpuSingle,    minCpuSingle, maxCpuSingle, false);
        }
    }

    private double minMax(double value, double min, double max, boolean invert) {
        if (value <= 0)  return 0.0;
        if (max == min)  return 0.5;
        double scaled = (value - min) / (max - min);
        scaled = Math.clamp(scaled, 0.0, 1.0);
        return invert ? 1.0 - scaled : scaled;
    }

    private void printStats() {
        System.out.printf("  battery:    %.1f → %.1f Wh%n",    minBattery,   maxBattery);
        System.out.printf("  weight:     %.2f → %.2f kg%n",    minWeight,    maxWeight);
        System.out.printf("  screen:     %.1f → %.1f inch%n",  minScreen,    maxScreen);
        System.out.printf("  pixels:     %.0f → %.0f%n",       minPixels,    maxPixels);
        System.out.printf("  gpuScore:   %.0f → %.0f%n",       minGpuScore,  maxGpuScore);
        System.out.printf("  cpuMulti:   %.0f → %.0f%n",       minCpuMulti,  maxCpuMulti);
        System.out.printf("  cpuSingle:  %.0f → %.0f%n",       minCpuSingle, maxCpuSingle);
    }
}