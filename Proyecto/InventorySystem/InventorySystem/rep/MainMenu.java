package rep;

import javax.swing.*;
import java.awt.*;

public class MainMenu extends JFrame {

    private final LoginScreen loginScreen; // Reference to the LoginScreen instance

    public MainMenu(LoginScreen loginScreen) {
        this.loginScreen = loginScreen;   // Save the reference to LoginScreen

        setTitle("Aeternum 3D - Main Menu");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Main panel for buttons
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new GridLayout(5, 1, 20, 20));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        menuPanel.setBackground(new Color(45, 52, 54)); // Dark background

        // Buttons for each section
        JButton productInventoryButton = createStyledButton("Product Inventory");
        JButton salesInventoryButton = createStyledButton("Sales Inventory");
        JButton statisticsButton = createStyledButton("Statistics");
        JButton logOutButton = createStyledButton("Log Out");

        JLabel companyNameLabel = new JLabel("Company Name: Aeternum 3D", JLabel.CENTER);
        companyNameLabel.setForeground(Color.blue);
        companyNameLabel.setFont(new Font("SansSerif", Font.BOLD, 16));

        // Add buttons and company label to the panel
        menuPanel.add(productInventoryButton);
        menuPanel.add(salesInventoryButton);
        menuPanel.add(statisticsButton);
        menuPanel.add(logOutButton);

        // Add panel and company label to the main frame
        add(menuPanel, BorderLayout.CENTER);
        add(companyNameLabel, BorderLayout.SOUTH);

        // Button actions
        productInventoryButton.addActionListener(e -> {
            new ProductInventory(this, null); // Open Product Inventory
            dispose();
        });

        salesInventoryButton.addActionListener(e -> {
            new SalesInventory(this, null); // Open Sales Inventory
            dispose();
        });

        statisticsButton.addActionListener(e -> openStatisticsDialog());

        logOutButton.addActionListener(e -> {
            loginScreen.setVisible(true); // Return to LoginScreen on logout
            dispose();
        });

        setVisible(true);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 16));
        button.setBackground(new Color(41, 128, 185)); // Blue color
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        return button;
    }

    private void openStatisticsDialog() {
        JDialog statisticsDialog = new JDialog(this, "Statistics", true);
        statisticsDialog.setSize(600, 400);
        statisticsDialog.setLocationRelativeTo(this);
        statisticsDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // Create a new Statistics panel and add it to the dialog
        Statistics statisticsPanel = new Statistics(this); // Pass MainMenu as the parent
        statisticsDialog.add(statisticsPanel); // Add the panel directly

        statisticsDialog.setVisible(true);
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginScreen::new);
    }
}
