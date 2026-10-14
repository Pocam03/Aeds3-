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
