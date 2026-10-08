package gui;

import javax.swing.*;
import java.util.Locale;

/**
 * Aspetto grafico comune: look and feel Nimbus ed etichette dei dialog in italiano,
 * indipendentemente dalla lingua del sistema operativo.
 */
public final class Aspetto {

    private Aspetto() {
    }

    public static void applica() {
        Locale.setDefault(Locale.ITALY);
        JComponent.setDefaultLocale(Locale.ITALY);
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | UnsupportedLookAndFeelException e) {
            // resta il look and feel predefinito
        }
        UIManager.put("OptionPane.okButtonText", "OK");
        UIManager.put("OptionPane.cancelButtonText", "Annulla");
        UIManager.put("OptionPane.yesButtonText", "Sì");
        UIManager.put("OptionPane.noButtonText", "No");
    }
}
