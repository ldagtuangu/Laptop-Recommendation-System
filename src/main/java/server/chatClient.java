package server;

import UI.*;

import com.google.gson.*;
import java.net.*;
import java.io.*;

/**
 * TCP Client — adapted từ chat app
 * Giữ nguyên structure: Socket + BufferedReader + PrintWriter
 * Thay nội dung: gửi RECOMMEND request thay vì chat message
 */
public class chatClient {

    private Socket        socket;
    private BufferedReader in;
    private PrintWriter   out;

    public chatClient(String host, int port) throws Exception {
        socket = new Socket(host, port);
        in     = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out    = new PrintWriter(socket.getOutputStream(), true);
    }

    /**
     * Khởi động client — đọc username prompt + start UI
     * Giữ nguyên từ chat app
     */
    public void start(ChatUI ui, String username) throws Exception {
        // Đọc "Enter your name:" từ server
        in.readLine();

        // Gửi username
        out.println(username);

        // Đọc welcome message
        String welcome = in.readLine();
        ui.appendMessage(welcome);

        // Thread đọc response từ server
        Thread readThread = new Thread(() -> {
            try {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    if (line.equals("END")) {
                        // Response hoàn chỉnh → display
                        String fullResponse = sb.toString();
                        ui.displayResponse(fullResponse);
                        sb.setLength(0);
                    } else {
                        sb.append(line).append("\n");
                    }
                }
            } catch (IOException e) {
                ui.appendMessage("Lost connection to server.");
            }
        });
        readThread.setDaemon(true);
        readThread.start();
    }

    /**
     * Gửi recommendation request
     */
    public void sendRecommend(String category, String cpuBrand,
                              Boolean wantGpu,
                              double weightPerf, double weightPort,
                              double weightDisp, int topN) {
        JsonObject req = new JsonObject();
        req.addProperty("category",          category);
        req.addProperty("cpuBrand",          cpuBrand);
        if (wantGpu != null) req.addProperty("wantGpu", wantGpu);
        else                 req.add("wantGpu", JsonNull.INSTANCE);
        req.addProperty("weightPerformance", weightPerf);
        req.addProperty("weightPortability", weightPort);
        req.addProperty("weightDisplay",     weightDisp);
        req.addProperty("topN",             topN);

        out.println("RECOMMEND " + new Gson().toJson(req));
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public void disconnect() {
        try {
            out.println("exit");
            socket.close();
        } catch (IOException ignored) {}
    }
}