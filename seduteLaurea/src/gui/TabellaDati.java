package gui;

import model.StatoRichiesta;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * JTable in sola lettura. La prima colonna contiene l'ID dell'elemento:
 * resta nel model (per sapere cosa è selezionato) ma non viene mostrata.
 */
class TabellaDati extends JTable {

    private static final Color VERDE = new Color(0x1E7B34);
    private static final Color ARANCIONE = new Color(0xB25E00);
    private static final Color ROSSO = new Color(0xB3261E);

    private final DefaultTableModel modello;

    TabellaDati(String... colonne) {
        String[] conId = new String[colonne.length + 1];
        conId[0] = "ID";
        System.arraycopy(colonne, 0, conId, 1, colonne.length);

        modello = new DefaultTableModel(conId, 0) {
            @Override
            public boolean isCellEditable(int riga, int colonna) {
                return false;
            }
        };
        setModel(modello);
        removeColumn(getColumnModel().getColumn(0)); // nasconde la colonna ID
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setAutoCreateRowSorter(true);
        setFillsViewportHeight(true);
        setRowHeight(24);
        getTableHeader().setReorderingAllowed(false);
        setDefaultRenderer(Object.class, new RendererStato());
    }

    void svuota() {
        modello.setRowCount(0);
    }

    void aggiungiRiga(String id, Object... valori) {
        Object[] riga = new Object[valori.length + 1];
        riga[0] = id;
        System.arraycopy(valori, 0, riga, 1, valori.length);
        modello.addRow(riga);
    }

    // ID della riga selezionata, oppure null se non c'è selezione
    String getIdSelezionato() {
        int riga = getSelectedRow();
        return riga < 0 ? null : (String) modello.getValueAt(convertRowIndexToModel(riga), 0);
    }

    // Colora le celle che contengono uno stato di richiesta
    private static class RendererStato extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable tabella, Object valore, boolean selezionata,
                                                       boolean focus, int riga, int colonna) {
            Object testo = valore instanceof StatoRichiesta stato ? GuiUtils.statoLeggibile(stato) : valore;
            Component c = super.getTableCellRendererComponent(tabella, testo, selezionata, focus, riga, colonna);
            if (!selezionata) {
                c.setForeground(valore instanceof StatoRichiesta stato ? colore(stato) : tabella.getForeground());
            }
            return c;
        }

        private static Color colore(StatoRichiesta stato) {
            return switch (stato) {
                case APPROVATA -> VERDE;
                case IN_ATTESA -> ARANCIONE;
                case RIFIUTATA -> ROSSO;
            };
        }
    }
}
