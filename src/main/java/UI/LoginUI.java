package UI;

import server.*;

import javax.swing.*;
import java.awt.*;

public class LoginUI extends JFrame {

    private JTextField  nameField;
    private JTextField  hostField;
    private JComboBox<String> categoryBox;
    private JComboBox<String> brandBox;
    private JComboBox<String> gpuBox;
    private JButton     connectButton;

    public LoginUI() {
        setTitle("Laptop Recommender — Connect");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("Your name:"));
        nameField = new JTextField();
        panel.add(nameField);

        panel.add(new JLabel("Host (IP server):"));
        hostField = new JTextField("localhost");
        panel.add(hostField);

        panel.add(new JLabel("Category:"));
        categoryBox = new JComboBox<>(new String[]{"any", "gaming", "office", "creative"});
        panel.add(categoryBox);

        panel.add(new JLabel("CPU Brand:"));
        brandBox = new JComboBox<>(new String[]{"any", "AMD", "Intel", "Apple"});
        panel.add(brandBox);

        panel.add(new JLabel("Need GPU?"));
        gpuBox = new JComboBox<>(new String[]{"don't care", "yes", "no"});
        panel.add(gpuBox);

        connectButton = new JButton("Connect & Get Recommendations");
        panel.add(new JLabel());
        panel.add(connectButton);

        add(panel);

        pack();
        setMinimumSize(getSize());

        connectButton.addActionListener(e -> connect());
        nameField.addActionListener(e -> connect());
    }

    private void connect() {
        String name = nameField.getText().trim();
        String host = hostField.getText().trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your name!");
            return;
        }

        try {
            chatClient client = new chatClient(host, 6767);
            ChatUI chatUI     = new ChatUI(client, name,
                    (String) categoryBox.getSelectedItem(),
                    (String) brandBox.getSelectedItem(),
                    (String) gpuBox.getSelectedItem());
            chatUI.setVisible(true);
            this.dispose();
            client.start(chatUI, name);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Cannot connect to server at " + host + ":6767");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginUI().setVisible(true));
    }
}