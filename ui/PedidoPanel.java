package ui;

import model.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

/**
 * Espelha MenuPedidos.menu(boolean admin) do console: Admin pode buscar,
 * listar todos, listar por CPF e cancelar — mas não inclui nem altera
 * pedido. Cliente pode buscar, listar por CPF, fazer novo pedido, alterar
 * (jogos/cupom) e cancelar — mas não tem "listar todos".
 */
public class PedidoPanel extends JPanel {
    private final boolean admin;
    private final PedidoDAO pedidoDAO;
    private final ClienteDAO clienteDAO;
    private final JogoDAO jogoDAO;
    private final CupomDAO cupomDAO;
    private final DefaultTableModel modeloTabela;
    private final JTable tabela;

    public PedidoPanel(boolean admin) throws Exception {
        this.admin = admin;
        pedidoDAO = new PedidoDAO();
        clienteDAO = new ClienteDAO();
        jogoDAO = new JogoDAO();
        cupomDAO = new CupomDAO();

        setLayout(new BorderLayout(6, 6));

        String[] colunas = {"ID", "Cliente", "Qtd. Jogos", "Cupom", "Valor Final", "Data"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tabela = new JTable(modeloTabela);
        add(new JScrollPane(tabela), BorderLayout.CENTER);

        JButton btnBuscarId = new JButton("Buscar por ID");
        JButton btnBuscarCpf = new JButton("Buscar por CPF do cliente");
        btnBuscarId.addActionListener(e -> buscarPorId());
        btnBuscarCpf.addActionListener(e -> buscarPorCpf());

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT));
        botoes.add(btnBuscarId);
        botoes.add(btnBuscarCpf);

        if (admin) {
            JButton btnListarTodos = new JButton("Listar todos");
            btnListarTodos.addActionListener(e -> recarregar());
            botoes.add(btnListarTodos);
        } else {
            JButton btnNovoPedido = new JButton("Novo Pedido");
            JButton btnEditar = new JButton("Editar (jogos/cupom)");
            btnNovoPedido.addActionListener(e -> novoPedido());
            btnEditar.addActionListener(e -> editar());
            botoes.add(btnNovoPedido);
            botoes.add(btnEditar);
        }

        JButton btnVerJogos = new JButton("Ver jogos do pedido");
        btnVerJogos.addActionListener(e -> verJogosDoPedido());
        botoes.add(btnVerJogos);

        JButton btnExcluir = new JButton("Cancelar pedido");
        btnExcluir.addActionListener(e -> excluir());
        botoes.add(btnExcluir);

        add(botoes, BorderLayout.NORTH);

        // admin enxerga tudo de cara ("listar todos" é uma operação dele);
        // cliente não tem essa opção no console, então a tabela começa vazia
        // até ele buscar por ID ou CPF.
        if (admin) recarregar();
    }

    private void recarregar() {
        try {
            preencherTabela(pedidoDAO.listarPedidos());
        } catch (Exception ex) {
            UiUtil.erro(this, "Erro ao listar pedidos", ex);
        }
    }

    private void preencherTabela(ArrayList<Pedido> pedidos) throws Exception {
        modeloTabela.setRowCount(0);
        for (Pedido p : pedidos) {
            adicionarLinha(p);
        }
    }

    private void adicionarLinha(Pedido p) throws Exception {
        Cliente cliente = clienteDAO.buscarCliente(p.getIdCliente());
        String nomeCliente = cliente != null ? cliente.getNome() : "ID " + p.getIdCliente();

        String cupomDescricao = "Nenhum";
        if (p.getIdCupom() != -1) {
            Cupom cupom = cupomDAO.buscarCupom(p.getIdCupom());
            cupomDescricao = cupom != null ? cupom.getCodigo() : "ID " + p.getIdCupom();
        }

        modeloTabela.addRow(new Object[]{
                p.getId(), nomeCliente, p.getIdJogos().size(), cupomDescricao,
                String.format("R$ %.2f", p.getValorFinal()), p.getData()
        });
    }

    private int idSelecionado() {
        int linha = tabela.getSelectedRow();
        if (linha == -1) return -1;
        return (int) modeloTabela.getValueAt(linha, 0);
    }

    private void novoPedido() {
        try {
            PedidoFormDialog dialog = new PedidoFormDialog(janela(), clienteDAO, jogoDAO, cupomDAO, null);
            dialog.setVisible(true);
            if (!dialog.isConfirmado()) return;

            Pedido criado = pedidoDAO.incluirPedido(dialog.getIdCliente(), dialog.getIdJogos(), dialog.getIdCupom());
            if (criado != null) {
                UiUtil.info(this, "Sucesso", "Pedido incluído com sucesso. Valor final: R$ "
                        + String.format("%.2f", criado.getValorFinal()));
                preencherTabela(pedidoDAO.listarPedidosPorCliente(criado.getIdCliente()));
            } else {
                UiUtil.erro(this, "Erro", "Não foi possível incluir o pedido.");
            }
        } catch (Exception ex) {
            UiUtil.erro(this, "Erro ao incluir pedido", ex);
        }
    }

    private void editar() {
        int id = idSelecionado();
        if (id == -1) {
            UiUtil.erro(this, "Nenhuma seleção", "Selecione um pedido na tabela.");
            return;
        }
        try {
            Pedido pedido = pedidoDAO.buscarPedido(id);
            if (pedido == null) {
                UiUtil.erro(this, "Não encontrado", "Pedido não encontrado.");
                return;
            }

            PedidoFormDialog dialog = new PedidoFormDialog(janela(), clienteDAO, jogoDAO, cupomDAO, pedido);
            dialog.setVisible(true);
            if (!dialog.isConfirmado()) return;

            pedido.setIdJogos(dialog.getIdJogos());
            pedido.setIdCupom(dialog.getIdCupom());

            if (pedidoDAO.alterarPedido(pedido)) {
                UiUtil.info(this, "Sucesso", "Pedido alterado com sucesso. Novo valor final: R$ "
                        + String.format("%.2f", pedido.getValorFinal()));
                preencherTabela(pedidoDAO.listarPedidosPorCliente(pedido.getIdCliente()));
            } else {
                UiUtil.erro(this, "Erro", "Não foi possível alterar o pedido.");
            }
        } catch (Exception ex) {
            UiUtil.erro(this, "Erro ao alterar pedido", ex);
        }
    }

    /**
     * Mostra o lado N:N do pedido: cada jogo do pedido (Pedido_Jogo) com o
     * título e o preço gravados na compra, comparados com o catálogo atual.
     */
    private void verJogosDoPedido() {
        int id = idSelecionado();
        if (id == -1) {
            UiUtil.erro(this, "Nenhuma seleção", "Selecione um pedido na tabela.");
            return;
        }
        try {
            Pedido pedido = pedidoDAO.buscarPedido(id);
            if (pedido == null) {
                UiUtil.erro(this, "Não encontrado", "Pedido não encontrado.");
                return;
            }
            java.util.List<Object[]> linhas = new ArrayList<>();
            for (PedidoJogo item : pedido.getItens()) {
                Jogo atual = jogoDAO.buscarJogo(item.getIdJogo());
                linhas.add(new Object[]{
                        item.getIdJogo(), item.getTitulo(), item.getQuantidade(),
                        String.format("R$ %.2f", item.getPreco()),
                        String.format("R$ %.2f", item.getSubtotal()),
                        atual == null ? "Excluído do catálogo" : "No catálogo",
                        atual == null ? "-" : String.format("R$ %.2f", atual.getPreco())
                });
            }
            UiUtil.mostrarTabela(this, "Jogos do pedido " + id,
                    "Pedido " + id + " — valor final R$ " + String.format("%.2f", pedido.getValorFinal())
                            + " (dados gravados na compra)",
                    new String[]{"ID Jogo", "Título (na compra)", "Qtd.", "Preço unit. pago", "Subtotal", "Situação", "Preço atual"}, linhas);
        } catch (Exception ex) {
            UiUtil.erro(this, "Erro ao carregar jogos do pedido", ex);
        }
    }

    private void excluir() {
        int id = idSelecionado();
        if (id == -1) {
            UiUtil.erro(this, "Nenhuma seleção", "Selecione um pedido na tabela.");
            return;
        }
        if (!UiUtil.confirmar(this, "Confirma a exclusão (cancelamento) do pedido " + id + "?")) return;

        try {
            if (pedidoDAO.excluirPedido(id)) {
                UiUtil.info(this, "Sucesso", "Pedido excluído com sucesso.");
                if (admin) recarregar(); else modeloTabela.setRowCount(0);
            } else {
                UiUtil.erro(this, "Erro", "Não foi possível excluir o pedido.");
            }
        } catch (Exception ex) {
            UiUtil.erro(this, "Erro ao excluir pedido", ex);
        }
    }

    private void buscarPorId() {
        String idStr = JOptionPane.showInputDialog(this, "ID do pedido:");
        if (idStr == null || idStr.trim().isEmpty()) return;

        try {
            int id = Integer.parseInt(idStr.trim());
            Pedido p = pedidoDAO.buscarPedido(id);
            modeloTabela.setRowCount(0);
            if (p == null) {
                UiUtil.info(this, "Busca", "Nenhum pedido encontrado com esse ID.");
            } else {
                adicionarLinha(p);
            }
        } catch (NumberFormatException ex) {
            UiUtil.erro(this, "ID inválido", "Digite um número inteiro.");
        } catch (Exception ex) {
            UiUtil.erro(this, "Erro ao buscar pedido", ex);
        }
    }

    private void buscarPorCpf() {
        String cpf = JOptionPane.showInputDialog(this, "CPF do cliente:");
        if (cpf == null || cpf.trim().isEmpty()) return;

        try {
            Cliente cliente = clienteDAO.buscarClientePorCpf(cpf.trim());
            if (cliente == null) {
                UiUtil.info(this, "Busca", "Nenhum cliente encontrado com esse CPF.");
                return;
            }
            // usa o índice em Árvore B+ sobre a FK Pedido.idCliente
            preencherTabela(pedidoDAO.listarPedidosPorCliente(cliente.getId()));
        } catch (Exception ex) {
            UiUtil.erro(this, "Erro ao buscar pedidos do cliente", ex);
        }
    }

    private Frame janela() {
        return (Frame) SwingUtilities.getWindowAncestor(this);
    }
}
