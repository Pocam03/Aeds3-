package ui;

import javax.swing.*;

/**
 * Espelha controller.Principal: pergunta o perfil de acesso (Admin/Cliente)
 * antes de abrir a tela correspondente, igual PrincipalView.exibirMenuPerfil().
 */
public class MainUI {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignorada) {
            // segue com o look and feel padrão do Swing se o do sistema não estiver disponível
        }

        SwingUtilities.invokeLater(MainUI::escolherPerfil);
    }

    private static void escolherPerfil() {
        Object[] opcoes = {"Admin", "Cliente", "Sair"};
        int escolha = JOptionPane.showOptionDialog(null,
                "Escolha o perfil de acesso:", "Loja de Jogos — AEDs III",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, opcoes, opcoes[0]);

        try {
            if (escolha == 0) {
                new TelaPrincipal().setVisible(true);
            } else if (escolha == 1) {
                new TelaCliente().setVisible(true);
            }
            // escolha == 2 (Sair) ou janela fechada: não abre nada
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Não foi possível iniciar a aplicação: " + e.getMessage(),
                    "Erro de inicialização", JOptionPane.ERROR_MESSAGE);
        }
    }
}
