package server;

import com.google.gson.*;
import recommender.*;
import java.net.*;
import java.io.*;
import java.util.*;

public class ClientHandler implements Runnable {

    private Socket socket;
    private PrintWriter out;
    private String username;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public ClientHandler(Socket socket){
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            
            out.println("Enter your name:");
            username = in.readLine();
            out.println("Welcome " + username);
            
            String message;
            while((message = in.readLine()) != null) {
                if(message.equals("EXIT")) break;
                
                if(message.startsWith("RECOMMEND ")) {
                    String json = message.substring("RECOMMEND ".length());
                    String response = handleRecommend(json);
                    out.println(response);
                    out.println("END");
                } else if (message.equals("HELP")) {
                    out.println(buildHelp());
                    out.println("END");
                } else if (message.equals("CACHE_STATS")) {
                    Server.cache.printStats();
                    out.println("Cache stats printed on server");
                    out.println("END");
                } else {
                    out.println("Unknown command. Send HELP for usage.");
                    out.println("END");
                }
            }
        } catch (IOException e) {
            System.out.println(username + " lost connection.");
        } finally {
            Server.clients.remove(this);
            System.out.println(username + " disconnected.");
            try { socket.close(); } catch (IOException ignored) {}
        }
    }

    private String handleRecommend(String requestJson) {

        JsonObject req = JsonParser.parseString(requestJson).getAsJsonObject();        String  category   = getStr(req, "category",           "any");

        String  cpuBrand   = getStr(req, "cpuBrand",           "any");
        Boolean wantGpu    = req.has("wantGpu") && !req.get("wantGpu").isJsonNull()
                ? req.get("wantGpu").getAsBoolean() : null;
        double  weightPerf = getDbl(req, "weightPerformance",  0.4);
        double  weightPort = getDbl(req, "weightPortability",   0.3);
        double  weightDisp = getDbl(req, "weightDisplay",       0.3);
        int     topN       = req.has("topN") ? req.get("topN").getAsInt() : 5;

        String cacheKey = RecommendCache.buildKey(category, cpuBrand, wantGpu,
                weightPerf, weightPort, weightDisp, topN);

        // ── Bước 1: Check Cache ───────────────────────────────────────────
        Optional<String> cached = Server.cache.get(cacheKey);
        if (cached.isPresent()) {
            System.out.println("[CACHE HIT] " + cacheKey);
            return cached.get().replace("\"cached\": false", "\"cached\": true");
        }

        // ── Bước 2: Check Database ────────────────────────────────────────
        Optional<String> fromDb = Server.db.findResult(cacheKey);
        if (fromDb.isPresent()) {
            System.out.println("[DB HIT] " + cacheKey);
            // Load từ DB → bỏ vào cache cho lần sau
            Server.cache.put(cacheKey, fromDb.get());
            return fromDb.get().replace("\"cached\": false", "\"cached\": true");
        }

        UserPreference pref = new UserPreference();
        pref.category          = category;
        pref.cpuBrand          = cpuBrand;
        pref.wantGpu           = wantGpu;
        pref.weightPerformance = weightPerf;
        pref.weightPortability = weightPort;
        pref.weightDisplay     = weightDisp;
        pref.topN              = topN;
        pref.normalize();

        // ── Bước 3: Tính mới ──────────────────────────────────────────────
        System.out.println("[COMPUTE] " + cacheKey);
        List<RecommendResult> results = Server.engine.recommend(pref);
        String response = buildResponse(results);

        // Lưu vào cả cache lẫn DB
        Server.cache.put(cacheKey, response);
        Server.db.saveResult(cacheKey, response);

        return response;
    }

    private String buildResponse(List<RecommendResult> results) {
        JsonObject response = new JsonObject();
        response.addProperty("status", "ok");
        response.addProperty("cached", false);

        JsonArray arr = new JsonArray();
        for (int i = 0; i < results.size(); i++) {
            RecommendResult r   = results.get(i);
            JsonObject      item = new JsonObject();

            item.addProperty("rank",        i + 1);
            item.addProperty("name",        r.laptop.name);
            item.addProperty("category",    r.laptop.category);
            item.addProperty("score",       Math.round(r.score * 1000.0) / 1000.0);
            item.addProperty("explanation", r.explanation);

            JsonArray tags = new JsonArray();
            if (r.laptop.tags != null) r.laptop.tags.forEach(tags::add);
            item.add("tags", tags);

            JsonArray similar = new JsonArray();
            if (r.similarLaptops != null)
                r.similarLaptops.forEach(d -> similar.add(d.name));
            item.add("similar", similar);

            arr.add(item);
        }

        response.add("results", arr);
        return gson.toJson(response);
    }

    private String buildHelp() {
        return """
            === LAPTOP RECOMMENDATION SERVER ===
            Commands:
              RECOMMEND <json>  → Get laptop recommendations
              CACHE_STATS       → Show cache statistics
              EXIT              → Disconnect
              
            JSON format:
            {
              "category":           "gaming|office|creative|any",
              "cpuBrand":           "AMD|Intel|Apple|any",
              "wantGpu":            true|false|null,
              "weightPerformance":  0.0-1.0,
              "weightPortability":  0.0-1.0,
              "weightDisplay":      0.0-1.0,
              "topN":               5
            }
            """;
    }

    private String  getStr(JsonObject o, String key, String def) {
        return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsString() : def;
    }
    private double  getDbl(JsonObject o, String key, double def) {
        return o.has(key) ? o.get(key).getAsDouble() : def;
    }

    public void sendMessage(String message) {
        out.println(message);
    }

}
