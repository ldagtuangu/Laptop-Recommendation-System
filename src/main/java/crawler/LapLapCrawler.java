package crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileWriter;
import java.util.*;

public class LapLapCrawler {

    public static void main(String[] args) {
        List<Laptop> laptops = new ArrayList<>();

        try {
            String baseUrl = "https://laplap.tech/";

            System.out.println("Connecting to site...");
            Document doc = Jsoup.connect(baseUrl)
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();
            System.out.println("Connected");

            Elements cards = doc.select(".flex.items-center");

            System.out.println("Found: " + cards.size() + " items");

            for (Element card : cards) {
                try {
                    String name = card.select(".text-3xl text-white").text();
                    String cpu = card.select("p:contains(CPU) + p.specs-value").text();
                    String gpu = doc.select("p:contains(GPU chính) + p.specs-value").text();
                    String battery = doc.select("p:contains(Dung lượng pin) + p.specs-value").text();
                    String weight = doc.select("p:contains(Khối lượng) + p.specs-value").text();
                    String screenSize = doc.select("p:contains(Kích thước màn hình) + p.specs-value").text();
                    String resolution = doc.select("p:contains(Độ phân giải) + p.specs-value").text();
                    String link = "https://laplap.tech/" + doc.select("a").attr("href");

                    Laptop laptop = new Laptop(
                            name, cpu, gpu, battery,
                            weight, screenSize, resolution, link
                    );

                    laptops.add(laptop);
                    System.out.println(laptop.toCSV());

                    Thread.sleep(800);

                } catch (Exception e) {
                    System.out.println("Error parsing item...");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
