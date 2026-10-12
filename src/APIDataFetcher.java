/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */

import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class APIDataFetcher extends JFrame {

    private JLabel lblTitle;
    private JLabel lblStatus;
    private JTextArea txtJoke;
    private JButton btnFetch;
    private JButton btnClear;

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public APIDataFetcher() {
        setTitle("API Data Fetcher");
        setSize(650, 480);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        mainPanel.setBackground(new Color(245, 248, 252));

        // Header
        lblTitle = new JLabel("Random Joke Fetcher", SwingConstants.CENTER);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 25));
        lblTitle.setForeground(new Color(23, 54, 93));
        mainPanel.add(lblTitle, BorderLayout.NORTH);

        // Joke display area
        txtJoke = new JTextArea();
        txtJoke.setFont(new Font("SansSerif", Font.PLAIN, 16));
        txtJoke.setLineWrap(true);
        txtJoke.setWrapStyleWord(true);
        txtJoke.setEditable(false);
        txtJoke.setText("Click 'Fetch Joke' to get a joke from the public API.");
        txtJoke.setMargin(new Insets(15, 15, 15, 15));

        JScrollPane scrollPane = new JScrollPane(txtJoke);
        scrollPane.setBorder(BorderFactory.createTitledBorder("API Response"));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Buttons and status
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setOpaque(false);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        buttonPanel.setOpaque(false);

        btnFetch = new JButton("Fetch Joke");
        btnFetch.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnFetch.setBackground(new Color(25, 118, 210));
        btnFetch.setForeground(Color.WHITE);
        btnFetch.setFocusPainted(false);

        btnClear = new JButton("Clear");
        btnClear.setFont(new Font("SansSerif", Font.PLAIN, 14));

        buttonPanel.add(btnFetch);
        buttonPanel.add(btnClear);

        lblStatus = new JLabel("Ready", SwingConstants.CENTER);
        lblStatus.setForeground(Color.DARK_GRAY);

        bottomPanel.add(buttonPanel, BorderLayout.CENTER);
        bottomPanel.add(lblStatus, BorderLayout.SOUTH);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Button actions
        btnFetch.addActionListener(e -> fetchJoke());

        btnClear.addActionListener(e -> {
            txtJoke.setText("");
            lblStatus.setText("Cleared");
        });
    }

    private void fetchJoke() {
        btnFetch.setEnabled(false);
        lblStatus.setText("Fetching data from API...");
        txtJoke.setText("Please wait...");

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {

                URI apiUri = URI.create(
                        "https://official-joke-api.appspot.com/random_joke");

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(apiUri)
                        .timeout(Duration.ofSeconds(15))
                        .header("Accept", "application/json")
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(
                        request,
                        HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    throw new Exception(
                            "API returned HTTP status: "
                                    + response.statusCode());
                }

                String json = response.body();

                String setup = extractJsonValue(json, "setup");
                String punchline = extractJsonValue(json, "punchline");

                if (setup == null || punchline == null) {
                    throw new Exception(
                            "Could not read joke data from API response.");
                }

                return "Here's your joke!\n\n"
                        + setup + "\n\n"
                        + punchline;
            }

            @Override
            protected void done() {
                btnFetch.setEnabled(true);

                try {
                    txtJoke.setText(get());
                    lblStatus.setText("Data fetched successfully!");
                } catch (Exception ex) {
                    txtJoke.setText(
                            "Unable to fetch data.\n\n"
                            + "Please check your internet connection "
                            + "and try again.\n\n"
                            + "Details: " + ex.getMessage());

                    lblStatus.setText("Request failed");
                }
            }
        };

        worker.execute();
    }

    // Extract a string value from the API's JSON response
    private String extractJsonValue(String json, String key) {
        String regex = "\"" + Pattern.quote(key)
                + "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"";

        Matcher matcher = Pattern.compile(regex).matcher(json);

        if (matcher.find()) {
            return unescapeJson(matcher.group(1));
        }

        return null;
    }

    // Handle common JSON escape sequences
    private String unescapeJson(String value) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);

            if (ch == '\\' && i + 1 < value.length()) {
                char next = value.charAt(++i);

                switch (next) {
                    case '"': result.append('"'); break;
                    case '\\': result.append('\\'); break;
                    case '/': result.append('/'); break;
                    case 'n': result.append('\n'); break;
                    case 'r': result.append('\r'); break;
                    case 't': result.append('\t'); break;
                    case 'b': result.append('\b'); break;
                    case 'f': result.append('\f'); break;
                    case 'u':
                        if (i + 4 < value.length()) {
                            String hex = value.substring(i + 1, i + 5);
                            try {
                                result.append((char) Integer.parseInt(hex, 16));
                                i += 4;
                            } catch (NumberFormatException ex) {
                                result.append("\\u");
                            }
                        } else {
                            result.append("\\u");
                        }
                        break;
                    default:
                        result.append(next);
                }
            } else {
                result.append(ch);
            }
        }

        return result.toString();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new APIDataFetcher().setVisible(true);
        });
    }
}
