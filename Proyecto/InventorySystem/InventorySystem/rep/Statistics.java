package rep;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Statistics extends JPanel {

    private JTable salesTable, costTable, profitTable;
    private DefaultTableModel salesModel, costModel, profitModel;

    public Statistics(JFrame parent) {
        initStatisticsPanel(parent);
    }

    private void initStatisticsPanel(JFrame parent) {
        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel salesPanel = new JPanel(new BorderLayout());
        salesModel = new DefaultTableModel(new String[]{"Date", "Invoice No.", "Name", "Description", "Sale"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Disable cell editing for Sales Report
            }
        };
        salesTable = createStyledTable(salesModel);
        salesPanel.add(new JScrollPane(salesTable), BorderLayout.CENTER);
        tabbedPane.add("Sales Report", salesPanel);

        JPanel costPanel = new JPanel(new BorderLayout());
        costModel = new DefaultTableModel(new String[]{"Date", "Invoice No.", "Name", "Description", "Code", "Grams Used", "Cost per Gram", "Total Cost"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Disable cell editing for Cost Report
            }
        };
        costTable = createStyledTable(costModel);
        costPanel.add(new JScrollPane(costTable), BorderLayout.CENTER);
        tabbedPane.add("Cost Report", costPanel);

        JPanel profitPanel = new JPanel(new BorderLayout());
        profitModel = new DefaultTableModel(new String[]{"Date", "Invoice No.", "Name", "Description", "Sale", "Total Cost", "Profit"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Disable cell editing for Profit Report
            }
        };
        profitTable = createStyledTable(profitModel);
        profitPanel.add(new JScrollPane(profitTable), BorderLayout.CENTER);
        tabbedPane.add("Profit Report", profitPanel);

        JButton returnButton = createStyledButton("Return to Main Menu");

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(236, 240, 241));
        buttonPanel.add(returnButton);

        add(tabbedPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        returnButton.addActionListener(e -> {
            parent.setVisible(true);
            SwingUtilities.getWindowAncestor(this).dispose(); // Close the dialog
        });

        loadSalesData();
        loadCostData();
        calculateProfit();
    }

    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(new Font("SansSerif", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(41, 128, 185));
        table.getTableHeader().setForeground(Color.WHITE);
        return table;
    }

    private void loadSalesData() {
        String sql = "SELECT date, invoiceNumber, customerName, description, price FROM sales";
        try (Connection conn = DatabaseHelper.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Object[] sale = {
                        rs.getDate("date"),
                        rs.getString("invoiceNumber"),
                        rs.getString("customerName"),
                        rs.getString("description"),
                        rs.getDouble("price")
                };
                salesModel.addRow(sale);
            }
        } catch (SQLException e) {
            System.out.println("Error loading sales data: " + e.getMessage());
        }
    }

    private void loadCostData() {
        String sql = "SELECT p.date, s.invoiceNumber, s.customerName, s.description, " +
                "p.code, s.gramsUsed, p.costPerGram, (s.gramsUsed * p.costPerGram) AS totalCost " +
                "FROM products p " +
                "JOIN sales s ON p.code = s.productCode";

        try (Connection conn = DatabaseHelper.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Object[] cost = {
                        rs.getDate("date"),
                        rs.getString("invoiceNumber"),
                        rs.getString("customerName"),
                        rs.getString("description"),
                        rs.getString("code"),
                        rs.getDouble("gramsUsed"),
                        rs.getDouble("costPerGram"),
                        rs.getDouble("totalCost")
                };
                costModel.addRow(cost);
            }
        } catch (SQLException e) {
            System.out.println("Error loading cost data: " + e.getMessage());
        }
    }

    private void calculateProfit() {
        double totalSales = 0;
        double totalCost = 0;

        for (int i = 0; i < salesModel.getRowCount(); i++) {
            totalSales += (double) salesModel.getValueAt(i, 4);
        }

        for (int i = 0; i < costModel.getRowCount(); i++) {
            totalCost += (double) costModel.getValueAt(i, 7);
        }

        double profit = totalSales - totalCost;

        for (int i = 0; i < salesModel.getRowCount(); i++) {
            Object[] profitRow = {
                    salesModel.getValueAt(i, 0), // Date
                    salesModel.getValueAt(i, 1), // Invoice No.
                    salesModel.getValueAt(i, 2), // Customer Name
                    salesModel.getValueAt(i, 3), // Description
                    salesModel.getValueAt(i, 4), // Sale
                    totalCost,                   // Total Cost (from cost report)
                    profit                       // Calculated Profit
            };
            profitModel.addRow(profitRow);
        }

        // Add a final row showing the totals
        profitModel.addRow(new Object[]{"Total", "", "", "", totalSales, totalCost, profit});
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 16));
        button.setBackground(new Color(41, 128, 185));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(200, 40));
        return button;
    }
}
