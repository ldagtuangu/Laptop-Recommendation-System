package processor;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class CsvReader {

    public static List<LaptopData> read(String filePath) throws IOException {
        List<LaptopData> list = new ArrayList<>();

        try(BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath),
                        StandardCharsets.UTF_8))) {
            String headerLine = br.readLine();
            if(headerLine == null) return list;

            String line;
            int lineNum = 1;

            DataCleaner cleaner = new DataCleaner();
            while((line = br.readLine()) != null){
                lineNum++;
                line = line.trim();
                if(line.isEmpty()) continue;

                String[] cols = line.split(",", 11);

                if(cols.length < 11) {
                    System.out.println("WARNING line " + lineNum
                            + ": only " + cols.length + " columns, skipping → " + line);
                    continue;
                }

                LaptopData d = new LaptopData();
                d.name = cols[0].trim();
                d.cpuRaw = cols[1].trim();
                d.gpuRaw = cols[2].trim();
                d.gpuScoreRaw = cols[7].trim();
                d.cpuSingleRaw = cols[8].trim();
                d.cpuMultiRaw = cols[9].trim();
                d.link = cols[10].trim();

                d.batteryWh = DataCleaner.parseDouble(cols[3].trim(), "Wh");
                d.weightKg = DataCleaner.parseWeight(cols[4].trim());
                d.screenInch = DataCleaner.parseDouble(cols[5].trim(), "inch");

                int[] res = DataCleaner.parseResolution(cols[6].trim());
                d.resolutionW = res[0];
                d.resolutionH = res[1];
                d.totalPixels = (long) d.resolutionW * d.resolutionH;

                LaptopData cleaned = cleaner.clean(d);

                list.add(cleaned);

            }
        }

        System.out.println("CsvReader: loaded " + list.size() + " laptops from " + filePath);
        return list;

    }
}
