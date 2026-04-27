package UI;

import server.*;

import com.google.gson.*;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Chat UI — adapted từ chat app
 * Thay vì gửi text message → gửi recommendation request với sliders
 */
public class ChatUI extends JFrame {

    private JTextArea  resultArea;
    private JSlider    perfSlider;
    private JSlider    portSlider;
    private JSlider    dispSlider;
    private JSpinner   topNSpinner;
    private JButton    recommendButton;
    private JLabel     perfLabel;
    private JLabel     portLabel;
    private JLabel     dispLabel;

    private final chatClient client;
    private final String     category;
    private final String     cpuBrand;
    private final String     gpuPref;

    public ChatUI(chatClient client, String username,
                  String category, String cpuBrand, String gpuPref) {
        this.client   = client;
        this.category = category;
        this.cpuBrand = cpuBrand;
        this.gpuPref  = gpuPref;

        setTitle("Laptop Recommender — " + username
                + " | " + category + " | " + cpuBrand);
        setSize(650, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // ── Result area ───────────────────────────────────────────────────────
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(resultArea);

        // ── Controls panel ────────────────────────────────────────────────────
        JPanel controlPanel = new JPanel(new GridLayout(5, 2, 10, 8));
        controlPanel.setBorder(BorderFactory.createTitledBorder("Preferences"));

        // Performance weight slider
        perfLabel = new JLabel("Performance: 40%");
        perfSlider = new JSlider(0, 100, 40);
        perfSlider.addChangeListener(e -> updateLabels());
        controlPanel.add(perfLabel);
        controlPanel.add(perfSlider);

        // Portability weight slider
        portLabel = new JLabel("Portability: 30%");
        portSlider = new JSlider(0, 100, 30);
        portSlider.addChangeListener(e -> updateLabels());
        controlPanel.add(portLabel);
        controlPanel.add(portSlider);

        // Display weight slider
        dispLabel = new JLabel("Display: 30%");
        dispSlider = new JSlider(0, 100, 30);
        dispSlider.addChangeListener(e -> updateLabels());
        controlPanel.add(dispLabel);
        controlPanel.add(dispSlider);

        // Top N spinner
        controlPanel.add(new JLabel("Top N results:"));
        topNSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 20, 1));
        controlPanel.add(topNSpinner);

        // Recommend button
        recommendButton = new JButton("Get Recommendations");
        recommendButton.setBackground(new Color(70, 130, 180));
        recommendButton.setForeground(Color.WHITE);
        recommendButton.setFont(new Font("Arial", Font.BOLD, 13));
        controlPanel.add(new JLabel());
        controlPanel.add(recommendButton);

        // ── Layout ────────────────────────────────────────────────────────────
        add(scrollPane,   BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);

        // ── Actions ───────────────────────────────────────────────────────────
        recommendButton.addActionListener(e -> sendRecommend());

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                client.disconnect();
            }
        });

        appendMessage("Connected! Click 'Get Recommendations' to start.");
        appendMessage("Category: " + category + " | Brand: " + cpuBrand
                + " | GPU: " + gpuPref);
    }

    // ── Send recommendation request ───────────────────────────────────────────
    private void sendRecommend() {
        // Normalize weights to sum = 1.0
        double perf = perfSlider.getValue() / 100.0;
        double port = portSlider.getValue() / 100.0;
        double disp = dispSlider.getValue() / 100.0;
        double total = perf + port + disp;
        if (total == 0) total = 1.0;
        perf /= total; port /= total; disp /= total;

        Boolean wantGpu = switch (gpuPref) {
            case "yes" -> true;
            case "no"  -> false;
            default    -> null;
        };

        int topN = (Integer) topNSpinner.getValue();

        recommendButton.setEnabled(false);
        recommendButton.setText("Loading...");
        resultArea.setText("");
        appendMessage("Sending request to server...\n");

        // Gửi trong background thread để không block UI
        final double fPerf = perf, fPort = port, fDisp = disp;
        new Thread(() -> {
            client.sendRecommend(category, cpuBrand, wantGpu,
                    fPerf, fPort, fDisp, topN);
        }).start();
    }

    // ── Display JSON response ─────────────────────────────────────────────────
    public void displayResponse(String jsonResponse) {
        SwingUtilities.invokeLater(() -> {
            recommendButton.setEnabled(true);
            recommendButton.setText("Get Recommendations");

            try {
                JsonObject resp = JsonParser.parseString(jsonResponse).getAsJsonObject();

                if (!"ok".equals(resp.get("status").getAsString())) {
                    appendMessage("Error: " + resp.get("message").getAsString());
                    return;
                }

                boolean cached = resp.get("cached").getAsBoolean();
                appendMessage(cached ? "══ [CACHED] ══" : "══ [FRESH] ══");

                JsonArray results = resp.getAsJsonArray("results");
                for (JsonElement el : results) {
                    JsonObject r = el.getAsJsonObject();
                    appendMessage(String.format("\n#%d %s [%s] — score: %.3f",
                            r.get("rank").getAsInt(),
                            r.get("name").getAsString(),
                            r.get("category").getAsString(),
                            r.get("score").getAsDouble()));
                    appendMessage("   " + r.get("explanation").getAsString());

                    // Tags
                    List<String> tagList = new ArrayList<>();
                    r.getAsJsonArray("tags").forEach(t -> tagList.add(t.getAsString()));
                    appendMessage("   Tags: " + String.join(", ", tagList));

                    // Similar
                    List<String> simList = new ArrayList<>();
                    r.getAsJsonArray("similar").forEach(s -> simList.add(s.getAsString()));
                    if (!simList.isEmpty())
                        appendMessage("   Similar: " + String.join(" | ", simList));
                }

            } catch (Exception e) {
                appendMessage("Parse error: " + e.getMessage());
            }
        });
    }

    // ── Update weight labels ──────────────────────────────────────────────────
    private void updateLabels() {
        perfLabel.setText("Performance: " + perfSlider.getValue() + "%");
        portLabel.setText("Portability: " + portSlider.getValue() + "%");
        dispLabel.setText("Display: "     + dispSlider.getValue() + "%");
    }

    // ── Append message to result area ─────────────────────────────────────────
    public void appendMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            resultArea.append(message + "\n");
            resultArea.setCaretPosition(resultArea.getDocument().getLength());
        });
    }
}