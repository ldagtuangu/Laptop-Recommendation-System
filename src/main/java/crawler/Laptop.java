package crawler;

public class Laptop {
    String name;
    String cpu;
    String gpu;
    String battery;
    String weight;
    String screenSize;
    String resolution;
    String link;
    String gpuScore;

    public Laptop(String name, String cpu, String gpu, String battery,
                  String weight, String screenSize, String resolution, String gpuScore, String link){
        this.name = name;
        this.cpu = cpu;
        this.gpu = gpu;
        this.battery = battery;
        this.weight = weight;
        this.screenSize = screenSize;
        this.resolution = resolution;
        this.gpuScore = gpuScore;
        this.link = link;
    }

    public String toCSV() {
        return name + "," + cpu + "," + gpu + "," + battery + "," +
                weight + "," + screenSize + "," + resolution + "," + link;
    }
}
