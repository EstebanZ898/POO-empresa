package rep;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.itextpdf.text.Document;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class ProductInventory extends JFrame {

    private JComboBox<String> codeComboBox, typeComboBox, subvariantComboBox;
    private JTextField colorField, costPerUnitField;
    private JLabel costPerGramLabel, totalPurchasedLabel;
    private JTable purchaseTable;
    private DefaultTableModel tableModel;
    private final Statistics statistics; // Reference to Statistics for refreshing

    public ProductInventory(JFrame parent, Statistics statistics) {
        this.statistics = statistics; // Initialize with Statistics instance

        setTitle("Product Inventory - Aeternum 3D");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Populate code dropdown dynamically from Code Master table
        codeComboBox = new JComboBox<>();
        populateCodeDropdown();

        // Initialize other components
        typeComboBox = new JComboBox<>();
        typeComboBox.setEditable(true);
        subvariantComboBox = new JComboBox<>();
        subvariantComboBox.setEditable(true);
        colorField = createStyledTextField();
        costPerUnitField = createStyledTextField();
        costPerGramLabel = new JLabel("Cost per Gram: ");
        totalPurchasedLabel = new JLabel("Total Purchased: 0.00");

        String[] columns = {"Date", "Code", "Type", "Subvariant", "Color", "Unit Cost", "Cost per Gram", "Total Cost"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Prevent table cell editing
            }
        };
        purchaseTable = new JTable(tableModel);
        styleTable(purchaseTable);  // Apply styling to make the table visually appealing

        JScrollPane tableScrollPane = new JScrollPane(purchaseTable);
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.setBackground(new Color(236, 240, 241));

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Code:"), gbc);
        gbc.gridx = 1; formPanel.add(codeComboBox, gbc);
        gbc.gridx = 2; formPanel.add(new JLabel("Type:"), gbc);
        gbc.gridx = 3; formPanel.add(typeComboBox, gbc);
        gbc.gridy = 1; gbc.gridx = 0;
        formPanel.add(new JLabel("Subvariant:"), gbc);
        gbc.gridx = 1; formPanel.add(subvariantComboBox, gbc);
        gbc.gridx = 2; formPanel.add(new JLabel("Color:"), gbc);
        gbc.gridx = 3; formPanel.add(colorField, gbc);
        gbc.gridy = 2; gbc.gridx = 0;
        formPanel.add(new JLabel("Cost per Unit:"), gbc);
        gbc.gridx = 1; formPanel.add(costPerUnitField, gbc);
        gbc.gridx = 2; formPanel.add(costPerGramLabel, gbc);
        gbc.gridx = 3; formPanel.add(totalPurchasedLabel, gbc);

        JButton addButton = createStyledButton("Product Purchase");
        JButton pdfDownloadButton = createStyledButton("Download PDF");
        JButton returnButton = createStyledButton("Return");
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(addButton);
        buttonPanel.add(pdfDownloadButton);
        buttonPanel.add(returnButton);

        add(formPanel, BorderLayout.NORTH);
        add(tableScrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Load existing data
        loadProductsFromDatabase();

        // Action listeners
        addButton.addActionListener(e -> addPurchase());
        returnButton.addActionListener(e -> {
            parent.setVisible(true);
            dispose();
        });

        pdfDownloadButton.addActionListener(e -> downloadPDF());

        codeComboBox.addActionListener(e -> populateCodeMasterData((String) codeComboBox.getSelectedItem()));

        setVisible(true);
    }

    private void styleTable(JTable table) {
        table.setRowHeight(30);
        table.setFont(new Font("SansSerif", Font.PLAIN, 14));
        table.setGridColor(new Color(200, 200, 200));
        table.setShowGrid(true);

        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(41, 128, 185));
        table.getTableHeader().setForeground(Color.WHITE);
    }

    private void populateCodeDropdown() {
        String sql = "SELECT code FROM code_master";
        try (Connection conn = DatabaseHelper.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                codeComboBox.addItem(rs.getString("code"));
            }
        } catch (SQLException e) {
            System.out.println("Error loading codes: " + e.getMessage());
        }
    }

    private void populateCodeMasterData(String selectedCode) {
        String sql = "SELECT type, subvariant, color FROM code_master WHERE code = ?";
        try (Connection conn = DatabaseHelper.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, selectedCode);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                typeComboBox.setModel(new DefaultComboBoxModel<>(new String[]{rs.getString("type")}));
                subvariantComboBox.setModel(new DefaultComboBoxModel<>(new String[]{rs.getString("subvariant")}));
                colorField.setText(rs.getString("color"));
            }
        } catch (SQLException e) {
            System.out.println("Error loading code data: " + e.getMessage());
        }
    }

    private void addPurchase() {
        String code = (String) codeComboBox.getSelectedItem();
        String type = (String) typeComboBox.getSelectedItem();
        String subvariant = (String) subvariantComboBox.getSelectedItem();
        String color = colorField.getText();
        double costPerUnit = Double.parseDouble(costPerUnitField.getText());
        double costPerGram = costPerUnit / 1000;
        double totalCost = costPerUnit;

        String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        Object[] purchase = {currentDate, code, type, subvariant, color, costPerUnit, costPerGram, totalCost};
        tableModel.addRow(purchase);

        String sql = "INSERT INTO products (date, code, type, subvariant, color, costPerUnit, costPerGram, totalCost) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseHelper.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, currentDate);
            pstmt.setString(2, code);
            pstmt.setString(3, type);
            pstmt.setString(4, subvariant);
            pstmt.setString(5, color);
            pstmt.setDouble(6, costPerUnit);
            pstmt.setDouble(7, costPerGram);
            pstmt.setDouble(8, totalCost);
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Product added to database successfully!");
        } catch (SQLException e) {
            System.out.println("Error adding product to database: " + e.getMessage());
        }

        updateTotalPurchased(totalCost);
        costPerUnitField.setText(""); colorField.setText("");
    }

    private void loadProductsFromDatabase() {
        String sql = "SELECT date, code, type, subvariant, color, costPerUnit, costPerGram, totalCost FROM products";
        try (Connection conn = DatabaseHelper.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Object[] product = {
                        rs.getString("date"),
                        rs.getString("code"),
                        rs.getString("type"),
                        rs.getString("subvariant"),
                        rs.getString("color"),
                        rs.getDouble("costPerUnit"),
                        rs.getDouble("costPerGram"),
                        rs.getDouble("totalCost")
                };
                tableModel.addRow(product);
            }
        } catch (SQLException e) {
            System.out.println("Error loading products from database: " + e.getMessage());
        }
    }

    private void updateTotalPurchased(double amount) {
        double totalPurchased = Double.parseDouble(totalPurchasedLabel.getText().split(": ")[1]);
        totalPurchased += amount;
        totalPurchasedLabel.setText("Total Purchased: " + String.format("%.2f", totalPurchased));
    }

    private void downloadPDF() {
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream("ProductInventory.pdf"));
            document.open();

            PdfPTable pdfTable = new PdfPTable(tableModel.getColumnCount());
            for (int i = 0; i < tableModel.getColumnCount(); i++) {
                PdfPCell headerCell = new PdfPCell(new Phrase(tableModel.getColumnName(i)));
                headerCell.setBackgroundColor(new com.itextpdf.text.BaseColor(41, 128, 185));
                headerCell.setPhrase(new Phrase(tableModel.getColumnName(i), new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.BOLD, com.itextpdf.text.BaseColor.WHITE)));
                pdfTable.addCell(headerCell);
            }

            for (int row = 0; row < tableModel.getRowCount(); row++) {
                for (int col = 0; col < tableModel.getColumnCount(); col++) {
                    String cellValue = tableModel.getValueAt(row, col).toString();
                    pdfTable.addCell(new Phrase(cellValue));
                }
            }

            document.add(pdfTable);
            document.close();
            JOptionPane.showMessageDialog(this, "PDF downloaded successfully!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error in PDF download: " + e.getMessage());
        }
    }

    private JTextField createStyledTextField() {
        JTextField textField = new JTextField(10);
        textField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        textField.setBorder(BorderFactory.createLineBorder(new Color(52, 152, 219), 2));
        return textField;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 16));
        button.setBackground(new Color(41, 128, 185));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        return button;
    }
}
