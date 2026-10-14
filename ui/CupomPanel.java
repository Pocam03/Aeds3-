package ui;

import model.Cupom;
import model.CupomDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class CupomPanel extends JPanel {
    private final CupomDAO cupomDAO;
    private final DefaultTableModel modeloTabela;
    private final JTable tabela;

    public CupomPanel() throws Exception {
        cupomDAO = new CupomDAO();

        setLayout(new BorderLayout(6, 6));

        String[] colunas = {"ID", "Código", "Valor", "Porcentagem"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tabela = new JTable(modeloTabela);
        add(new JScrollPane(tabela), BorderLayout.CENTER);

        JButton btnAdicionar = new JButton("Adicionar");
        JButton btnEditar = new JButton("Editar");
        JButton btnExcluir = new JButton("Excluir");
        JButton btnBuscarId = new JButton("Buscar por ID");
        JButton btnAtualizar = new JButton("Listar todos");

        btnAdicionar.addActionListener(e -> adicionar());
        btnEditar.addActionListener(e -> editar());
        btnExcluir.addActionListener(e -> excluir());
        btnBuscarId.addActionListener(e -> buscarPorId());
        btnAtualizar.addActionListener(e -> recarregar());

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT));
        botoes.add(btnAdicionar);
        botoes.add(btnEditar);
        botoes.add(btnExcluir);
        botoes.add(btnBuscarId);
        botoes.add(btnAtualizar);
        add(botoes, BorderLayout.NORTH);

        recarregar();
    }

    private void recarregar() {
        try {
            modeloTabela.setRowCount(0);
            for (Cupom c : cupomDAO.listarCupons()) {
                adicionarLinha(c);
            }
        } catch (Exception ex) {
            UiUtil.erro(this, "Erro ao listar cupons", ex);
        }
    }

    private void adicionarLinha(Cupom c) {
        modeloTabela.addRow(new Object[]{c.getId(), c.getCodigo(), c.getValor(), c.getPorcentagem() + "%"});
    }

    private int idSelecionado() {
        int linha = tabela.getSelectedRow();
        if (linha == -1) return -1;
        return (int) modeloTabela.getValueAt(linha, 0);
    }

    private void adicionar() {
        CupomFormDialog dialog = new CupomFormDialog(janela(), null);
        dialog.setVisible(true);
        if (!dialog.isConfirmado()) return;

        try {
            if (cupomDAO.incluirCupom(dialog.getCupom())) {
                UiUtil.info(this, "Sucesso", "Cupom incluído com sucesso (ID " + dialog.getCupom().getId() + ").");
                recarregar();
            } else {
                UiUtil.erro(this, "Erro", "Não foi possível incluir o cupom.");
            }
        } catch (Exception ex) {
            UiUtil.erro(this, "Erro ao incluir cupom", ex);
        }
    }

    private void editar() {
        int id = idSelecionado();
        if (id == -1) {
            UiUtil.erro(this, "Nenhuma seleção", "Selecione um cupom na tabela.");
            return;
        }
        try {
            Cupom cupom = cupomDAO.buscarCupom(id);
            if (cupom == null) {
                UiUtil.erro(this, "Não encontrado", "Cupom não encontrado.");
                return;
            }
            CupomFormDialog dialog = new CupomFormDialog(janela(), cupom);
            dialog.setVisible(true);
            if (!dialog.isConfirmado()) return;

            if (cupomDAO.alterarCupom(dialog.getCupom())) {
                UiUtil.info(this, "Sucesso", "Cupom alterado com sucesso.");
                recarregar();
            } else {
                UiUtil.erro(this, "Erro", "Não foi possível alterar o cupom.");
            }
        } catch (Exception ex) {
            UiUtil.erro(this, "Erro ao alterar cupom", ex);
        }
    }

    private void excluir() {
        int id = idSelecionado();
        if (id == -1) {
            UiUtil.erro(this, "Nenhuma seleção", "Selecione um cupom na tabela.");
            return;
        }
        if (!UiUtil.confirmar(this, "Confirma a exclusão do cupom " + id + "?")) return;

        try {
            if (cupomDAO.excluirCupom(id)) {
                UiUtil.info(this, "Sucesso", "Cupom excluído com sucesso.");
                recarregar();
            } else {
                UiUtil.erro(this, "Erro", "Não foi possível excluir o cupom.");
            }
        } catch (Exception ex) {
            UiUtil.erro(this, "Erro ao excluir cupom", ex);
        }
    }

    private void buscarPorId() {
        String idStr = JOptionPane.showInputDialog(this, "ID do cupom:");
        if (idStr == null || idStr.trim().isEmpty()) return;

        try {
            int id = Integer.parseInt(idStr.trim());
            Cupom c = cupomDAO.buscarCupom(id);
            modeloTabela.setRowCount(0);
            if (c == null) {
                UiUtil.info(this, "Busca", "Nenhum cupom encontrado com esse ID.");
            } else {
                adicionarLinha(c);
            }
        } catch (NumberFormatException ex) {
            UiUtil.erro(this, "ID inválido", "Digite um número inteiro.");
        } catch (Exception ex) {
            UiUtil.erro(this, "Erro ao buscar cupom", ex);
        }
    }

    private Frame janela() {
        return (Frame) SwingUtilities.getWindowAncestor(this);
    }
}
