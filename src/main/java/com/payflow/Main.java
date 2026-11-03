
package com.payflow;

import com.payflow.database.DatabaseManager;
import com.payflow.view.LoginFrame;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {

    public static void main(String[] args) {

        try {
            // Initialize the database before opening the application.
            DatabaseManager.initializeDatabase();

            // Use the system's native appearance.
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName()
            );

            // Launch the Login Screen.
            SwingUtilities.invokeLater(LoginFrame::new);

        } catch (Exception e) {

            e.printStackTrace();

            SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(
                            null,
                            "PayFlow could not start:\n" + e.getMessage(),
                            "Startup Error",
                            JOptionPane.ERROR_MESSAGE
                    )
            );
        }
    }
}
