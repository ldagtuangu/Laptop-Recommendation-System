package database;

import jakarta.persistence.*;

@Entity
@Table(name = "laptops")
public class LaptopEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", length = 255)
    private String name;

    @Column(name = "cpu", length = 255)
    private String cpu;

    @Column(name = "gpu", length = 255)
    private String gpu;

    @Column(name = "link", length = 500)
    private String link;

    @Column(name = "battery_wh")
    private double batteryWh;

    @Column(name = "weight_kg")
    private double weightKg;

    @Column(name = "screen_inch")
    private double screenInch;

    @Column(name = "res_w")
    private int resW;

    @Column(name = "res_h")
    private int resH;

    @Column(name = "total_pixels")
    private long totalPixels;

    @Column(name = "gpu_score")
    private double gpuScore;

    @Column(name = "cpu_single")
    private double cpuSingle;

    @Column(name = "cpu_multi")
    private double cpuMulti;

    @Column(name = "has_discrete_gpu")
    private boolean hasDiscreteGpu;

    @Column(name = "is_amd")
    private boolean isAmd;

    @Column(name = "is_intel")
    private boolean isIntel;

    @Column(name = "is_apple")
    private boolean isApple;

    @Column(name = "is_gaming_gpu")
    private boolean isGamingGpu;

    @Column(name = "norm_battery")
    private double normBattery;

    @Column(name = "norm_weight")
    private double normWeight;

    @Column(name = "norm_screen")
    private double normScreen;

    @Column(name = "norm_resolution")
    private double normResolution;

    @Column(name = "norm_gpu_score")
    private double normGpuScore;

    @Column(name = "norm_cpu_multi")
    private double normCpuMulti;

    @Column(name = "norm_cpu_single")
    private double normCpuSingle;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "tags", columnDefinition = "TEXT")
    private String tags;     // lưu dạng "high GPU|lightweight|good battery"

    public static LaptopEntity from(processor.LaptopData d) {
        LaptopEntity e = new LaptopEntity();
        e.name           = d.name;
        e.cpu            = d.cpuRaw;
        e.gpu            = d.gpuRaw;
        e.link           = d.link;
        e.batteryWh      = d.batteryWh;
        e.weightKg       = d.weightKg;
        e.screenInch     = d.screenInch;
        e.resW           = d.resolutionW;
        e.resH           = d.resolutionH;
        e.totalPixels    = d.totalPixels;
        e.gpuScore       = d.gpuScore;
        e.cpuSingle      = d.cpuSingle;
        e.cpuMulti       = d.cpuMulti;
        e.hasDiscreteGpu = d.hasDiscreteGpu;
        e.isAmd          = d.isAmd;
        e.isIntel        = d.isIntel;
        e.isApple        = d.isApple;
        e.isGamingGpu    = d.isGamingGpu;
        e.normBattery    = d.normBattery;
        e.normWeight     = d.normWeight;
        e.normScreen     = d.normScreen;
        e.normResolution = d.normResolution;
        e.normGpuScore   = d.normGpuScore;
        e.normCpuMulti   = d.normCpuMulti;
        e.normCpuSingle  = d.normCpuSingle;
        e.category       = d.category;
        e.tags           = d.tags != null
                ? String.join("|", d.tags) : "";
        return e;
    }

    public Long getId()              { return id; }
    public String getName()          { return name; }
    public String getCpu()           { return cpu; }
    public String getGpu()           { return gpu; }
    public String getLink()          { return link; }
    public double getBatteryWh()     { return batteryWh; }
    public double getWeightKg()      { return weightKg; }
    public double getScreenInch()    { return screenInch; }
    public String getCategory()      { return category; }
    public String getTags()          { return tags; }
    public double getNormGpuScore()  { return normGpuScore; }
    public double getNormCpuMulti()  { return normCpuMulti; }
    public double getNormBattery()   { return normBattery; }
    public double getNormWeight()    { return normWeight; }
    public boolean isApple()         { return isApple; }
    public boolean isAmd()           { return isAmd; }
    public boolean isIntel()         { return isIntel; }
    public boolean isGamingGpu()     { return isGamingGpu; }
}
