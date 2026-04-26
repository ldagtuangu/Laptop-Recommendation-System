package recommender;

import processor.LaptopData;
import java.util.List;

public class RecommendResult {

    public LaptopData laptop;
    public double score;
    public String explanation;
    public List<LaptopData> similarLaptops;


    public RecommendResult(LaptopData laptop, double score, String explanation) {
        this.laptop = laptop;
        this.score = score;
        this.explanation = explanation;
    }
}
