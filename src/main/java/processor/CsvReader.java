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

            while((line = br.readLine()) != null){
                lineNum++;
                line = line.trim();
                if(line.isEmpty()) continue;

                String[] cols = line.split(",", 8);

                if(cols.length < 8) {
                    System.out.println("WARNING line " + lineNum
                            + ": only " + cols.length + " columns, skipping → " + line);
                    continue;
                }

                LaptopData d = new LaptopData();
                d.name = cols[0].trim();
                d.cpuRaw = cols[1].trim();
                d.link = cols[7].trim();

                d.batteryWh = DataCleaner.parseDouble(cols[3].trim(), "Wh");

            }
        }
    }
}
