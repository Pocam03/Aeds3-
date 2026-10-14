package ui;

import javax.swing.*;
import java.awt.*;

/**
 * Espelha PrincipalView.exibirMenuCliente() do console: o perfil Cliente só
 * tem acesso a Pedidos (buscar, listar por CPF, novo pedido, alterar
 * jogos/cupom, cancelar) — nada de Clientes/Jogos/Cupons.
 */
public class TelaCliente extends JFrame {

    public TelaCliente() throws Exception {
        super("Loja de Jogos — Cliente");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());
        add(new PedidoPanel(false), BorderLayout.CENTER);

        setMinimumSize(new Dimension(760, 480));
        setLocationRelativeTo(null);
    }
}
