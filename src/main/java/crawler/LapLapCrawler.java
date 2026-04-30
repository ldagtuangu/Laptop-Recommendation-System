package crawler;

import processor.LaptopData;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.FileWriter;
import java.time.Duration;
import java.util.*;

public class LapLapCrawler {

    public static void main(String[] args) {
        List<LaptopData> laptops = new ArrayList<>();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");

        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get("https://laplap.tech/");

            int maxClicks = 50;
            int clickCount = 0;
            while (clickCount < maxClicks) {
                try {
                    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
                    WebElement loadMoreBtn = wait.until(
                            ExpectedConditions.presenceOfElementLocated(
                                    By.xpath("//button[contains(text(),'Tải thêm')]")
                            )
                    );

                    // Lấy số item TRƯỚC khi click
                    int currentCount = driver.findElements(
                            By.cssSelector("a[href*='/device/']")
                    ).size();

                    // Scroll đến nút rồi click bằng JS
                    ((JavascriptExecutor) driver).executeScript(
                            "arguments[0].scrollIntoView(true);", loadMoreBtn
                    );
                    Thread.sleep(500);
                    ((JavascriptExecutor) driver).executeScript(
                            "arguments[0].click();", loadMoreBtn
                    );

                    long deadline = System.currentTimeMillis() + 5000;
                    while (System.currentTimeMillis() < deadline) {
                        int newCount = driver.findElements(
                                By.cssSelector("a[href*='/device/']")
                        ).size();
                        if (newCount > currentCount) {
                            System.out.println("Items: " + currentCount + " → " + newCount);
                            break;
                        }
                        Thread.sleep(300);
                    }
                } catch (Exception e) {
                    break;
                }
            }

            String fullHtml = driver.getPageSource();
            driver.quit();

            Document homePage = Jsoup.parse(fullHtml, "https://laplap.tech");

            Elements cards = homePage.select("a[href^=/device/]");
            System.out.println("Found: " + cards.size() + " items");

            for (Element card : cards) {
                try {

                    String devicePath = card.attr("href");
                    String deviceUrl = "https://laplap.tech" + devicePath;

                    Document doc = Jsoup.connect(deviceUrl)
                            .userAgent("Mozilla/5.0")
                            .timeout(10000)
                            .get();
                    String screenSize = getSpecValue(doc, "Kích thước màn hình");
                    if (size(screenSize) <= 11) {
                        continue;
                    }
                    String name = doc.select("h1").first() != null
                                ? doc.select("h1").first().text() : "";
                    if(name.contains("tab") || name.contains("Tab")){
                        continue;
                    }
                    String cpu        = getSpecValue(doc, "CPU");
                    String gpu        = getSpecValue(doc, "GPU chính");
                    String battery    = getSpecValue(doc, "Dung lượng pin");
                    String weight     = getSpecValue(doc, "Khối lượng");
                    String cpuMulti   = getSpecValue(doc, "Geekbench 6 có gắm sạc", "Geekbench 6 CPU Multi Core");
                    String cpuSingle  = getSpecValue(doc, "Geekbench 6 có gắm sạc", "Geekbench 6 CPU Single Core");
                    String gpuScore   = getSpecValue(doc, "Geekbench 6 có gắm sạc", "Geekbench 6 GPU");
                    String resolution = getSpecValue(doc, "Độ phân giải");

                    LaptopData laptop = new LaptopData(
                            name, cpu, gpu, battery,
                            weight, screenSize, resolution, cpuSingle, cpuMulti, gpuScore, deviceUrl
                    );

                    laptops.add(laptop);
                    System.out.println(laptop.toCSV());

                    Thread.sleep(800);

                } catch (Exception e) {
                    System.out.println("Error parsing item...");
                }
            }

            saveTOCSV(laptops);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static int size(String screenSize) {
        try {
            if (screenSize == null || screenSize.isEmpty()) return 0;

            String number = screenSize.split(" ")[0];  // "14.2"
            return (int) Double.parseDouble(number);   // parseDouble thay parseInt
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static String getSpecValue(Document doc, String label) {
        for (Element p : doc.select("p")) {
            if (p.text().trim().equals(label)) {
                Element next = p.nextElementSibling();
                if (next != null && next.tagName().equals("p")) {
                    return next.text().trim();
                }
            }
        }
        return "";
    }

    private static String getSpecValue(Document doc, String heading, String label) {
        boolean inSection = false;

        for(Element el : doc.getAllElements()){
            if(el.tagName().equals("h2") && el.text().contains(heading)){
                inSection = true;
                continue;
            }

            if(inSection && el.tagName().equals("h2")){
                inSection = false;
            }

            if(inSection && el.tagName().equals("p") && el.text().trim().equals(label)){
                Element next = el.nextElementSibling();
                if(next != null && next.tagName().equals("p")){
                    return next.text().trim();
                }
            }
        }
        return "";
    }

    public static void saveTOCSV(List<LaptopData> laptops){
        try{
            FileWriter writer = new FileWriter("laptop.csv");

            for(LaptopData l : laptops) {
                writer.append(l.toCSV()).append("\n");
            }

            writer.flush();
            writer.close();

            System.out.println("Saved to laptops.csv");
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}
