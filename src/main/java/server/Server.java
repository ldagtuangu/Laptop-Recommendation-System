package server;

import processor.*;
import recommender.*;
import database.*;
import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class Server {

    static List<ClientHandler> clients = new ArrayList<>();
    static RecommendationEngine engine;
    static RecommendCache cache = new RecommendCache();
    static DatabaseManager db;

    public static void main(String[] args) throws Exception {

        System.out.println("Loading laptop data...");
        List<LaptopData> all = CsvReader.read("laptop.csv");

        all.removeIf(d -> d.name.contains("Galaxy Tab"));

        DataCleaner cleaner = new DataCleaner();
        for (LaptopData d : all) cleaner.clean(d);

        Normalizer normalizer = new Normalizer();
        normalizer.fit(all);
        normalizer.normalize(all);

        all.removeIf(d -> d.cpuMulti  == 0
                || d.gpuScore  == 0
                || d.cpuSingle == 0);

        // ── K-Means ───────────────────────────────────────────────────────────
        KMeans kmeans = new KMeans();
        kmeans.fit(all);

        for (LaptopData d : all) d.tags = TagAssigner.assignTags(d);

        engine = new RecommendationEngine(all);
        System.out.println("Loaded " + all.size() + " laptops.");

        System.out.println("Connecting to database...");
        db = new DatabaseManager();
        db.saveAll(all);

        long count = db.countLaptops();
        if (count == 0) {
            db.saveAll(all);
            System.out.println("Saved " + all.size() + " laptops to DB.");
        } else {
            System.out.println("DB already has " + count + " laptops.");
        }


        db.printDbCacheStats();

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            cache.clearExpired();
            cache.printStats();
            db.printDbCacheStats();
        }, 60, 60, TimeUnit.SECONDS);

        ServerSocket serverSocket = new ServerSocket(6767);
        System.out.println("Server listening on port 6767");

        while(true) {
            Socket ck = serverSocket.accept();
            System.out.println("Client connected: " + ck.getInetAddress());

            ClientHandler handler = new ClientHandler(ck);
            clients.add(handler);
            new Thread(handler).start();
        }
    }

    static void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }
}
