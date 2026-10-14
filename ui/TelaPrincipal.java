package ui;

import javax.swing.*;
import java.awt.*;

public class TelaPrincipal extends JFrame {

    public TelaPrincipal() throws Exception {
        super("Loja de Jogos — Admin");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTabbedPane abas = new JTabbedPane();
        abas.addTab("Clientes", new ClientePanel());
        abas.addTab("Jogos", new JogoPanel());
        abas.addTab("Cupons", new CupomPanel());
        abas.addTab("Pedidos", new PedidoPanel(true));

        setLayout(new BorderLayout());
        add(abas, BorderLayout.CENTER);
        add(UiUtil.barraVoltar(this), BorderLayout.SOUTH);
        setMinimumSize(new Dimension(820, 520));
        setLocationRelativeTo(null);
    }
}
