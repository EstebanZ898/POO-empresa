package rep;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SalesInventory extends JFrame {

    private DefaultTableModel salesTableModel;
    private JTextField customerNameField, descriptionField, gramsUsedField, priceField, colorField;
    private JComboBox<String> codeComboBox, typeComboBox, invoiceNumberComboBox;
    private final Statistics statistics; // Reference to Statistics for refreshing

    public SalesInventory(JFrame parent, Statistics statistics) {
        this.statistics = statistics; // Initialize with Statistics instance

        setTitle("Sales Inventory - Aeternum 3D");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Colors for professional look
        Color primaryBlue = new Color(41, 128, 185);
        Color backgroundGray = new Color(236, 240, 241);
        Color whiteColor = Color.WHITE;

        // Initialize and style components
        customerNameField = createStyledTextField();
        descriptionField = createStyledTextField();
        gramsUsedField = createStyledTextField();
        priceField = createStyledTextField();
        colorField = createStyledTextField();
        colorField.setEditable(false);

        codeComboBox = new JComboBox<>();
        populateCodeDropdown();

        JButton addCodeButton = new JButton("+");
        addCodeButton.setBackground(primaryBlue);
        addCodeButton.setForeground(whiteColor);
        addCodeButton.addActionListener(e -> openCodeMasterDialog());

        JPanel codePanel = new JPanel(new BorderLayout());
        codePanel.add(codeComboBox, BorderLayout.CENTER);
        codePanel.add(addCodeButton, BorderLayout.EAST);

        typeComboBox = new JComboBox<>();
        typeComboBox.setEditable(false);

        invoiceNumberComboBox = new JComboBox<>(generateInvoiceNumbers());
        invoiceNumberComboBox.setFont(new Font("SansSerif", Font.BOLD, 16));

        String[] columns = {"Date", "Invoice No.", "Customer Name", "Description", "Code", "Type", "Grams Used", "Price"};
        salesTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;  // Prevent table cell editing
            }
        };

        JTable salesTable = new JTable(salesTableModel);
        styleTable(salesTable);
        JScrollPane tableScrollPane = new JScrollPane(salesTable);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(backgroundGray);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Add form fields in two-column format
        addFormField(formPanel, gbc, 0, 0, "Customer Name:", customerNameField);
        addFormField(formPanel, gbc, 0, 1, "Description:", descriptionField);
        addFormField(formPanel, gbc, 1, 0, "Code:", codePanel);
        addFormField(formPanel, gbc, 1, 1, "Type:", typeComboBox);
        addFormField(formPanel, gbc, 2, 0, "Color:", colorField);
        addFormField(formPanel, gbc, 2, 1, "Grams Used:", gramsUsedField);
        addFormField(formPanel, gbc, 3, 0, "Price:", priceField);
        addFormField(formPanel, gbc, 3, 1, "Invoice No:", invoiceNumberComboBox);

        JButton addButton = createStyledButton("Add Sale");
        JButton returnButton = createStyledButton("Return");
        addButton.setBackground(primaryBlue);
        addButton.setForeground(whiteColor);
        returnButton.setBackground(primaryBlue);
        returnButton.setForeground(whiteColor);

        addButton.addActionListener(e -> addSale());
        returnButton.addActionListener(e -> {
            parent.setVisible(true);
            dispose();
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(backgroundGray);
        buttonPanel.add(addButton);
        buttonPanel.add(returnButton);

        add(formPanel, BorderLayout.NORTH);
        add(tableScrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        loadSalesFromDatabase();

        codeComboBox.addActionListener(e -> populateCodeDetails((String) codeComboBox.getSelectedItem()));
        setVisible(true);
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, int col, String labelText, JComponent field) {
        gbc.gridx = col * 2;      // Set X position based on column (0 or 1)
        gbc.gridy = row;          // Set Y position based on row
        panel.add(new JLabel(labelText), gbc);
        gbc.gridx = col * 2 + 1;  // Position field to the right of the label
        panel.add(field, gbc);
    }

    private void openCodeMasterDialog() {
        CodeMasterDialog dialog = new CodeMasterDialog(this);
        dialog.setVisible(true);
        refreshCodeDropdown(); // or populateCodeDropdown(), depending on your implementation
    }

    private String[] generateInvoiceNumbers() {
        String[] invoiceNumbers = new String[10];
        for (int i = 0; i < 10; i++) {
            invoiceNumbers[i] = "F" + String.format("%04d", i + 1);
        }
        return invoiceNumbers;
    }

    private void populateCodeDropdown() {
        codeComboBox.removeAllItems();
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

    private void populateCodeDetails(String selectedCode) {
        String sql = "SELECT type, color FROM code_master WHERE code = ?";
        try (Connection conn = DatabaseHelper.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, selectedCode);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                typeComboBox.setModel(new DefaultComboBoxModel<>(new String[]{rs.getString("type")}));
                colorField.setText(rs.getString("color"));
            }
        } catch (SQLException e) {
            System.out.println("Error loading code details: " + e.getMessage());
        }
    }

    public void refreshCodeDropdown() {
        codeComboBox.removeAllItems();
        populateCodeDropdown(); // Re-populates the dropdown with updated data
    }

    private void addSale() {
        String customerName = customerNameField.getText();
        String description = descriptionField.getText();
        double gramsUsed = Double.parseDouble(gramsUsedField.getText());
        double price = Double.parseDouble(priceField.getText());
        String type = (String) typeComboBox.getSelectedItem();
        String invoiceNumber = (String) invoiceNumberComboBox.getSelectedItem();
        String code = (String) codeComboBox.getSelectedItem();

        String sql = "INSERT INTO sales (invoiceNumber, customerName, description, gramsUsed, price, date, type, productCode) VALUES (?, ?, ?, ?, ?, CURDATE(), ?, ?)";
        try (Connection conn = DatabaseHelper.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, invoiceNumber);
            pstmt.setString(2, customerName);
            pstmt.setString(3, description);
            pstmt.setDouble(4, gramsUsed);
            pstmt.setDouble(5, price);
            pstmt.setString(6, type);
            pstmt.setString(7, code);
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Sale added to database successfully!");
           // statistics.refreshData();
        } catch (SQLException e) {
            System.out.println("Error adding sale to database: " + e.getMessage());
        }

        Object[] sale = {java.time.LocalDate.now(), invoiceNumber, customerName, description, code, type, gramsUsed, price};
        salesTableModel.addRow(sale);

        customerNameField.setText("");
        descriptionField.setText("");
        gramsUsedField.setText("");
        priceField.setText("");
    }

    private void loadSalesFromDatabase() {
        String sql = "SELECT date, invoiceNumber, customerName, description, productCode AS code, type, gramsUsed, price FROM sales";
        try (Connection conn = DatabaseHelper.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Object[] sale = {
                        rs.getDate("date"),
                        rs.getString("invoiceNumber"),
                        rs.getString("customerName"),
                        rs.getString("description"),
                        rs.getString("code"),
                        rs.getString("type"),
                        rs.getDouble("gramsUsed"),
                        rs.getDouble("price")
                };
                salesTableModel.addRow(sale);
            }
        } catch (SQLException e) {
            System.out.println("Error loading sales from database: " + e.getMessage());
        }
    }

    private JTextField createStyledTextField() {
        JTextField textField = new JTextField(10);
        textField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        textField.setBackground(Color.WHITE);
        textField.setBorder(BorderFactory.createLineBorder(new Color(41, 128, 185), 2));
        return textField;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 16));
        button.setBackground(new Color(41, 128, 185));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(150, 40));
        return button;
    }

    private void styleTable(JTable table) {
        table.setRowHeight(30);
        table.setFont(new Font("SansSerif", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(41, 128, 185));
        table.getTableHeader().setForeground(Color.WHITE);
    }
}
