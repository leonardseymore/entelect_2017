package za.co.entelect.challenge.swing;

import javax.swing.*;

public class AppSwing {
    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        GUI app = new GUI();
        app.setVisible(true);
    }
}
