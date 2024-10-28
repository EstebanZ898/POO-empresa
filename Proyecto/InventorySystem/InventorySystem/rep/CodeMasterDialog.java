package rep;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

public class CodeMasterDialog extends JDialog {

    private JPanel codesPanel;
    private ArrayList<JComponent[]> codeFieldsList;
    private boolean multipleCodesAdded = false;

    public CodeMasterDialog(JFrame parent) {
        super(parent, "Add Codes", true);
        setSize(400, 300);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        // Panel to hold each row of fields vertically
        codesPanel = new JPanel();
        codesPanel.setLayout(new BoxLayout(codesPanel, BoxLayout.Y_AXIS));
        codeFieldsList = new ArrayList<>();

        addNewCodeFields();

        JScrollPane scrollPane = new JScrollPane(codesPanel);

        // Styled buttons
        JButton addMoreButton = createStyledButton("Add More");
        JButton readyButton = createStyledButton("Ready");

        addMoreButton.addActionListener(e -> addNewCodeFields());
        readyButton.addActionListener(e -> onReady());

        // Panel for buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addMoreButton);
        buttonPanel.add(readyButton);

        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void addNewCodeFields() {
        JPanel rowPanel = new JPanel(new GridBagLayout());
        rowPanel.setBackground(new Color(236, 240, 241)); // Light gray background

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        JTextField codeField = createStyledTextField();
        JTextField typeField = createStyledTextField();
        JTextField subvariantField = createStyledTextField();
        JTextField colorField = createStyledTextField();
        JTextField gramsUsedField = createStyledTextField();

        // Adding fields vertically
        rowPanel.add(new JLabel("Code:"), gbc);
        gbc.gridx = 1;
        rowPanel.add(codeField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        rowPanel.add(new JLabel("Type:"), gbc);
        gbc.gridx = 1;
        rowPanel.add(typeField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        rowPanel.add(new JLabel("Subvariant:"), gbc);
        gbc.gridx = 1;
        rowPanel.add(subvariantField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        rowPanel.add(new JLabel("Color:"), gbc);
        gbc.gridx = 1;
        rowPanel.add(colorField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        rowPanel.add(new JLabel("Grams Used:"), gbc);
        gbc.gridx = 1;
        rowPanel.add(gramsUsedField, gbc);

        codesPanel.add(rowPanel);
        codesPanel.revalidate();
        codesPanel.repaint();

        codeFieldsList.add(new JComponent[]{codeField, typeField, subvariantField, colorField, gramsUsedField});

        if (codeFieldsList.size() > 1) {
            multipleCodesAdded = true;
        }
    }

    private void onReady() {
        ArrayList<String[]> addedCodes = new ArrayList<>();

        for (JComponent[] fields : codeFieldsList) {
            String code = ((JTextField) fields[0]).getText().trim();
            String type = ((JTextField) fields[1]).getText().trim();
            String subvariant = ((JTextField) fields[2]).getText().trim();
            String color = ((JTextField) fields[3]).getText().trim();
            String gramsUsed = ((JTextField) fields[4]).getText().trim();

            if (validateFields(code, type, color)) {
                addedCodes.add(new String[]{code, type, subvariant, color, gramsUsed});
                addCodeToDatabase(code, type, subvariant.isEmpty() ? null : subvariant, color, gramsUsed);
            } else {
                JOptionPane.showMessageDialog(this, "Code, Type, and Color fields are required and cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return; // Exit without adding incomplete data
            }
        }

        if (multipleCodesAdded) {
            JOptionPane.showMessageDialog(this, "Multiple codes added successfully.");
        } else {
            JOptionPane.showMessageDialog(this, "Single code added successfully.");
        }

        ((SalesInventory) getParent()).refreshCodeDropdown();
        dispose();
    }

    private boolean validateFields(String code, String type, String color) {
        return !code.isEmpty() && !type.isEmpty() && !color.isEmpty();
    }

    private void addCodeToDatabase(String code, String type, String subvariant, String color, String gramsUsed) {
        String sql = "INSERT INTO code_master (code, type, subvariant, color) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseHelper.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, code);
            pstmt.setString(2, type);
            pstmt.setString(3, subvariant);
            pstmt.setString(4, color);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error adding code: " + e.getMessage());
        }
    }

    private JTextField createStyledTextField() {
        JTextField textField = new JTextField(10);
        textField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        textField.setBorder(BorderFactory.createLineBorder(new Color(41, 128, 185), 2)); // Blue border
        textField.setBackground(Color.WHITE);
        return textField;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setBackground(new Color(41, 128, 185)); // Blue background
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        return button;
    }
}