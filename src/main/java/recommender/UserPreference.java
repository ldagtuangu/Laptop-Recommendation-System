package recommender;

public class UserPreference {

    public String category = "any";
    public String cpuBrand = "any";
    public Boolean wantGpu = null;

    public double weightPerformance = 0.4;
    public double weightPortability = 0.3;
    public double weightDisplay = 0.3;

    public int topN = 5;

    public static UserPreference forGaming() {
        UserPreference p = new UserPreference();
        p.category = "gaming";
        p.wantGpu = true;
        p.weightPerformance = 0.7;
        p.weightPortability = 0.1;
        p.weightDisplay = 0.2;
        return p;
    }

    public static UserPreference forOffice() {
        UserPreference p = new UserPreference();
        p.category = "office";
        p.wantGpu = false;
        p.weightPerformance = 0.2;
        p.weightPortability = 0.6;
        p.weightDisplay = 0.2;
        return p;
    }

    public static UserPreference forCreative() {
        UserPreference p = new UserPreference();
        p.category = "creative";
        p.wantGpu = null;
        p.weightPerformance = 0.4;
        p.weightPortability = 0.2;
        p.weightDisplay = 0.4;
        return p;
    }

    public void normalize() {
        double total = weightDisplay + weightPortability + weightPerformance;
        if(total <= 0) total = 1.0;
        weightPerformance /= total;
        weightPortability /= total;
        weightDisplay /= total;
    }
    @Override
    public String toString() {
        return String.format(
                "UserPreference{category=%s, brand=%s, gpu=%s, " +
                        "perf=%.1f, port=%.1f, disp=%.1f, topN=%d}",
                category, cpuBrand, wantGpu,
                weightPerformance, weightPortability, weightDisplay, topN
        );
    }
}
