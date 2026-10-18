package ui;

import javax.swing.*;
import java.awt.*;

public class UiUtil {
    public static void erro(Component pai, String titulo, Exception e) {
        JOptionPane.showMessageDialog(pai, e.getMessage() != null ? e.getMessage() : e.toString(),
                titulo, JOptionPane.ERROR_MESSAGE);
    }

    public static void erro(Component pai, String titulo, String mensagem) {
        JOptionPane.showMessageDialog(pai, mensagem, titulo, JOptionPane.ERROR_MESSAGE);
    }

    public static void info(Component pai, String titulo, String mensagem) {
        JOptionPane.showMessageDialog(pai, mensagem, titulo, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void mostrarTabela(Component pai, String titulo, String cabecalho, String[] colunas, java.util.List<Object[]> linhas) {
        javax.swing.table.DefaultTableModel modelo = new javax.swing.table.DefaultTableModel(colunas, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        for (Object[] l : linhas) modelo.addRow(l);
        JPanel painel = new JPanel(new BorderLayout(6, 6));
        painel.add(new JLabel(cabecalho), BorderLayout.NORTH);
        JScrollPane rolagem = new JScrollPane(new JTable(modelo));
        rolagem.setPreferredSize(new Dimension(620, 220));
        painel.add(rolagem, BorderLayout.CENTER);
        JOptionPane.showMessageDialog(pai, painel, titulo, JOptionPane.PLAIN_MESSAGE);
    }

    public static boolean confirmar(Component pai, String mensagem) {
        return JOptionPane.showConfirmDialog(pai, mensagem, "Confirmação", JOptionPane.YES_NO_OPTION)
                == JOptionPane.YES_OPTION;
    }
        public static JPanel barraVoltar(JFrame janela) {
        JButton btnVoltar = new JButton("\u2190 Voltar (trocar perfil)");
        btnVoltar.addActionListener(e -> {
            janela.dispose();
            MainUI.escolherPerfil();
        });
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT));
        barra.add(btnVoltar);
        return barra;
    }
}
