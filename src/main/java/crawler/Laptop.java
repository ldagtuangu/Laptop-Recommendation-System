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

    public Laptop(String name, String cpu, String gpu, String battery,
                  String weight, String screenSize, String resolution, String link){
        this.name = name;
        this.cpu = cpu;
        this.gpu = gpu;
        this.battery = battery;
        this.weight = weight;
        this.screenSize = this.screenSize;
        this.resolution = resolution;
        this.link = link;
    }

    public String toCSV() {
        return name + "," + cpu + "," + gpu + "," + battery + "," +
                weight + "," + screenSize + "," + resolution + "," + link;
    }
}
