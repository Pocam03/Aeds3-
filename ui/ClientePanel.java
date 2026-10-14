package ui;

import model.Cliente;
import model.ClienteDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;

public class ClientePanel extends JPanel {
    private final ClienteDAO clienteDAO;
    private final DefaultTableModel modeloTabela;
    private final JTable tabela;

    public ClientePanel() throws Exception {
        clienteDAO = new ClienteDAO();

        setLayout(new BorderLayout(6, 6));

        String[] colunas = {"ID", "Nome", "CPF", "Nascimento", "Telefones"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tabela = new JTable(modeloTabela);
        add(new JScrollPane(tabela), BorderLayout.CENTER);

        JButton btnAdicionar = new JButton("Adicionar");
        JButton btnEditar = new JButton("Editar");
        JButton btnExcluir = new JButton("Excluir");
        JButton btnBuscarId = new JButton("Buscar por ID");
        JButton btnBuscarCpf = new JButton("Buscar por CPF");
        JButton btnAtualizar = new JButton("Listar todos");

        btnAdicionar.addActionListener(e -> adicionar());
        btnEditar.addActionListener(e -> editar());
        btnExcluir.addActionListener(e -> excluir());
        btnBuscarId.addActionListener(e -> buscarPorId());
        btnBuscarCpf.addActionListener(e -> buscarPorCpf());
        btnAtualizar.addActionListener(e -> recarregar());

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT));
        botoes.add(btnAdicionar);
        botoes.add(btnEditar);
        botoes.add(btnExcluir);
        botoes.add(btnBuscarId);
        botoes.add(btnBuscarCpf);
        botoes.add(btnAtualizar);
        add(botoes, BorderLayout.NORTH);

        recarregar();
    }

    private void recarregar() {
        try {
            modeloTabela.setRowCount(0);
            for (Cliente c : clienteDAO.listarClientes()) {
                adicionarLinha(c);
            }
        } catch (Exception ex) {
            UiUtil.erro(this, "Erro ao listar clientes", ex);
        }
    }

    private void adicionarLinha(Cliente c) {
        modeloTabela.addRow(new Object[]{
                c.getId(), c.getNome(), c.getCpf(),
                c.getNascimento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                String.join(", ", c.getTelefones())
        });
    }

    private int idSelecionado() {
        int linha = tabela.getSelectedRow();
        if (linha == -1) return -1;
        return (int) modeloTabela.getValueAt(linha, 0);
    }

    private void adicionar() {
        ClienteFormDialog dialog = new ClienteFormDialog(janela(), null);
        dialog.setVisible(true);
        if (!dialog.isConfirmado()) return;

        try {
            if (clienteDAO.incluirCliente(dialog.getCliente())) {
                UiUtil.info(this, "Sucesso", "Cliente incluído com sucesso (ID " + dialog.getCliente().getId() + ").");
                recarregar();
            } else {
                UiUtil.erro(this, "Erro", "Não foi possível incluir o cliente.");
            }
        } catch (Exception ex) {
            UiUtil.erro(this, "Erro ao incluir cliente", ex);
        }
    }

    private void editar() {
        int id = idSelecionado();
        if (id == -1) {
            UiUtil.erro(this, "Nenhuma seleção", "Selecione um cliente na tabela.");
            return;
        }
        try {
            Cliente cliente = clienteDAO.buscarCliente(id);
            if (cliente == null) {
                UiUtil.erro(this, "Não encontrado", "Cliente não encontrado.");
                return;
            }
            ClienteFormDialog dialog = new ClienteFormDialog(janela(), cliente);
            dialog.setVisible(true);
            if (!dialog.isConfirmado()) return;

            if (clienteDAO.alterarCliente(dialog.getCliente())) {
                UiUtil.info(this, "Sucesso", "Cliente alterado com sucesso.");
                recarregar();
            } else {
                UiUtil.erro(this, "Erro", "Não foi possível alterar o cliente.");
            }
        } catch (Exception ex) {
            UiUtil.erro(this, "Erro ao alterar cliente", ex);
        }
    }

    private void excluir() {
        int id = idSelecionado();
        if (id == -1) {
            UiUtil.erro(this, "Nenhuma seleção", "Selecione um cliente na tabela.");
            return;
        }
        if (!UiUtil.confirmar(this, "Confirma a exclusão do cliente " + id + "?")) return;

        try {
            if (clienteDAO.excluirCliente(id)) {
                UiUtil.info(this, "Sucesso", "Cliente excluído com sucesso.");
                recarregar();
            } else {
                UiUtil.erro(this, "Erro", "Não foi possível excluir o cliente.");
            }
        } catch (Exception ex) {
            UiUtil.erro(this, "Erro ao excluir cliente", ex);
        }
    }

    private void buscarPorId() {
        String idStr = JOptionPane.showInputDialog(this, "ID do cliente:");
        if (idStr == null || idStr.trim().isEmpty()) return;

        try {
            int id = Integer.parseInt(idStr.trim());
            Cliente c = clienteDAO.buscarCliente(id);
            modeloTabela.setRowCount(0);
            if (c == null) {
                UiUtil.info(this, "Busca", "Nenhum cliente encontrado com esse ID.");
            } else {
                adicionarLinha(c);
            }
        } catch (NumberFormatException ex) {
            UiUtil.erro(this, "ID inválido", "Digite um número inteiro.");
        } catch (Exception ex) {
            UiUtil.erro(this, "Erro ao buscar cliente", ex);
        }
    }

    private void buscarPorCpf() {
        String cpf = JOptionPane.showInputDialog(this, "CPF do cliente:");
        if (cpf == null || cpf.trim().isEmpty()) return;

        try {
            Cliente c = clienteDAO.buscarClientePorCpf(cpf.trim());
            modeloTabela.setRowCount(0);
            if (c == null) {
                UiUtil.info(this, "Busca", "Nenhum cliente encontrado com esse CPF.");
            } else {
                adicionarLinha(c);
            }
        } catch (Exception ex) {
            UiUtil.erro(this, "Erro ao buscar cliente", ex);
        }
    }

    private Frame janela() {
        return (Frame) SwingUtilities.getWindowAncestor(this);
    }
}
