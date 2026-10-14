package ui;

import model.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PedidoFormDialog extends JDialog {
    private final ClienteDAO clienteDAO;
    private final JogoDAO jogoDAO;
    private final CupomDAO cupomDAO;

    private final boolean modoEdicao;
    private final Pedido pedido;
    private Integer idClienteResolvido;

    private final JTextField campoCpf = new JTextField(14);
    private final JLabel labelCliente = new JLabel(" ");
    private final JTextField campoCupom = new JTextField(12);

    private final DefaultListModel<JogoItem> modeloDisponiveis = new DefaultListModel<>();
    private final JList<JogoItem> listaDisponiveis = new JList<>(modeloDisponiveis);
    private final DefaultListModel<JogoItem> modeloNoPedido = new DefaultListModel<>();
    private final JList<JogoItem> listaNoPedido = new JList<>(modeloNoPedido);

    private boolean confirmado = false;

    private static class JogoItem {
        final int id;
        final String titulo;
        final float preco;

        JogoItem(Jogo j) {
            this.id = j.getId();
            this.titulo = j.getTitulo();
            this.preco = j.getPreco();
        }

        public String toString() {
            return id + " - " + titulo + " (R$ " + String.format("%.2f", preco) + ")";
        }
    }

    public PedidoFormDialog(Frame pai, ClienteDAO clienteDAO, JogoDAO jogoDAO, CupomDAO cupomDAO, Pedido pedidoExistente) throws Exception {
        super(pai, pedidoExistente == null ? "Novo Pedido" : "Editar Pedido", true);
        this.clienteDAO = clienteDAO;
        this.jogoDAO = jogoDAO;
        this.cupomDAO = cupomDAO;
        this.modoEdicao = pedidoExistente != null;
        this.pedido = pedidoExistente;

        montarLayout();
        carregarJogosDisponiveis();

        if (modoEdicao) {
            prepararModoEdicao();
        }

        pack();
        setLocationRelativeTo(pai);
    }

    private void montarLayout() {
        setLayout(new BorderLayout(6, 6));

        JPanel topo = new JPanel(new GridLayout(0, 1, 4, 4));
        topo.setBorder(BorderFactory.createEmptyBorder(10, 10, 4, 10));

        JPanel linhaCliente = new JPanel(new BorderLayout(4, 4));
        linhaCliente.add(new JLabel("CPF do cliente:"), BorderLayout.WEST);
        linhaCliente.add(campoCpf, BorderLayout.CENTER);
        JButton btnBuscarCliente = new JButton("Buscar");
        btnBuscarCliente.addActionListener(e -> buscarCliente());
        linhaCliente.add(btnBuscarCliente, BorderLayout.EAST);
        topo.add(linhaCliente);
        topo.add(labelCliente);

        JPanel linhaCupom = new JPanel(new BorderLayout(4, 4));
        linhaCupom.add(new JLabel("Código do cupom (vazio = sem cupom):"), BorderLayout.WEST);
        linhaCupom.add(campoCupom, BorderLayout.CENTER);
        topo.add(linhaCupom);

        add(topo, BorderLayout.NORTH);

        JPanel centro = new JPanel(new GridLayout(1, 2, 8, 8));
        centro.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

        JPanel painelDisponiveis = new JPanel(new BorderLayout(4, 4));
        painelDisponiveis.setBorder(BorderFactory.createTitledBorder("Jogos disponíveis"));
        listaDisponiveis.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        painelDisponiveis.add(new JScrollPane(listaDisponiveis), BorderLayout.CENTER);
        JButton btnAdicionarJogo = new JButton("Adicionar >>");
        btnAdicionarJogo.addActionListener(e -> adicionarJogosSelecionados());
        painelDisponiveis.add(btnAdicionarJogo, BorderLayout.SOUTH);
        centro.add(painelDisponiveis);

        JPanel painelNoPedido = new JPanel(new BorderLayout(4, 4));
        painelNoPedido.setBorder(BorderFactory.createTitledBorder("Jogos no pedido"));
        painelNoPedido.add(new JScrollPane(listaNoPedido), BorderLayout.CENTER);
        JButton btnRemoverJogo = new JButton("<< Remover");
        btnRemoverJogo.addActionListener(e -> removerJogoSelecionado());
        painelNoPedido.add(btnRemoverJogo, BorderLayout.SOUTH);
        centro.add(painelNoPedido);

        add(centro, BorderLayout.CENTER);

        JButton btnSalvar = new JButton("Salvar");
        JButton btnCancelar = new JButton("Cancelar");
        btnSalvar.addActionListener(e -> salvar());
        btnCancelar.addActionListener(e -> dispose());
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        painelBotoes.add(btnSalvar);
        painelBotoes.add(btnCancelar);
        add(painelBotoes, BorderLayout.SOUTH);

        setMinimumSize(new Dimension(560, 420));
    }

    private void carregarJogosDisponiveis() throws Exception {
        for (Jogo j : jogoDAO.listarJogos()) {
            modeloDisponiveis.addElement(new JogoItem(j));
        }
    }

    private void prepararModoEdicao() {
        campoCpf.setEnabled(false);
        campoCpf.setText("(não é possível trocar o cliente de um pedido já existente)");

        try {
            Cliente cliente = clienteDAO.buscarCliente(pedido.getIdCliente());
            idClienteResolvido = pedido.getIdCliente();
            labelCliente.setText("Cliente: " + (cliente != null ? cliente.getNome() : "ID " + pedido.getIdCliente()));

            for (int idJogo : pedido.getIdJogos()) {
                Jogo j = jogoDAO.buscarJogo(idJogo);
                if (j != null) modeloNoPedido.addElement(new JogoItem(j));
            }

            if (pedido.getIdCupom() != -1) {
                Cupom cupom = cupomDAO.buscarCupom(pedido.getIdCupom());
                if (cupom != null) campoCupom.setText(cupom.getCodigo());
            }
        } catch (Exception ex) {
            UiUtil.erro(this, "Erro ao carregar pedido", ex);
        }
    }

    private void buscarCliente() {
        try {
            Cliente cliente = clienteDAO.buscarClientePorCpf(campoCpf.getText().trim());
            if (cliente == null) {
                idClienteResolvido = null;
                labelCliente.setText("Cliente não encontrado para esse CPF.");
            } else {
                idClienteResolvido = cliente.getId();
                labelCliente.setText("Cliente: " + cliente.getNome() + " (ID " + cliente.getId() + ")");
            }
        } catch (Exception ex) {
            UiUtil.erro(this, "Erro ao buscar cliente", ex);
        }
    }

    private void adicionarJogosSelecionados() {
        for (JogoItem item : listaDisponiveis.getSelectedValuesList()) {
            modeloNoPedido.addElement(item);
        }
    }

    private void removerJogoSelecionado() {
        int idx = listaNoPedido.getSelectedIndex();
        if (idx >= 0) modeloNoPedido.remove(idx);
    }

    private void salvar() {
        if (!modoEdicao && idClienteResolvido == null) {
            UiUtil.erro(this, "Cliente não resolvido", "Busque um cliente válido pelo CPF antes de salvar.");
            return;
        }

        if (modeloNoPedido.isEmpty()) {
            UiUtil.erro(this, "Nenhum jogo", "Adicione pelo menos um jogo ao pedido.");
            return;
        }

        String codigoCupom = campoCupom.getText().trim();
        Integer idCupomResolvido = -1;
        if (!codigoCupom.isEmpty()) {
            try {
                Cupom cupom = cupomDAO.buscarCupomPorCodigo(codigoCupom);
                if (cupom == null) {
                    UiUtil.erro(this, "Cupom não encontrado", "Nenhum cupom com esse código. Deixe em branco pra não usar cupom.");
                    return;
                }
                idCupomResolvido = cupom.getId();
            } catch (Exception ex) {
                UiUtil.erro(this, "Erro ao buscar cupom", ex);
                return;
            }
        }

        List<Integer> idJogos = new ArrayList<>();
        for (int i = 0; i < modeloNoPedido.size(); i++) idJogos.add(modeloNoPedido.get(i).id);

        this.idJogosFinal = idJogos;
        this.idCupomFinal = idCupomResolvido;
        confirmado = true;
        dispose();
    }

    private List<Integer> idJogosFinal;
    private Integer idCupomFinal;

    public boolean isConfirmado() {
        return confirmado;
    }

    public int getIdCliente() {
        return modoEdicao ? pedido.getIdCliente() : idClienteResolvido;
    }

    public ArrayList<Integer> getIdJogos() {
        return new ArrayList<>(idJogosFinal);
    }

    public int getIdCupom() {
        return idCupomFinal;
    }

    public Pedido getPedidoExistente() {
        return pedido;
    }
}
