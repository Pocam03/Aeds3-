package ui;

import model.Jogo;
import model.JogoDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class JogoPanel extends JPanel {
    private final JogoDAO jogoDAO;
    private final DefaultTableModel modeloTabela;
    private final JTable tabela;

    public JogoPanel() throws Exception {
        jogoDAO = new JogoDAO();

        setLayout(new BorderLayout(6, 6));

        String[] colunas = {"ID", "Título", "Desenvolvedora", "Classificação", "Preço", "Ano"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tabela = new JTable(modeloTabela);
        add(new JScrollPane(tabela), BorderLayout.CENTER);

        JButton btnAdicionar = new JButton("Adicionar");
        JButton btnEditar = new JButton("Editar");
        JButton btnExcluir = new JButton("Excluir");
        JButton btnBuscarId = new JButton("Buscar por ID");
        JButton btnOrdenar = new JButton("Ordenar por preço");
        JButton btnAtualizar = new JButton("Atualizar lista");

        btnAdicionar.addActionListener(e -> adicionar());
        btnEditar.addActionListener(e -> editar());
        btnExcluir.addActionListener(e -> excluir());
        btnBuscarId.addActionListener(e -> buscarPorId());
        btnOrdenar.addActionListener(e -> ordenarPorPreco());
        btnAtualizar.addActionListener(e -> recarregar());

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT));
        botoes.add(btnAdicionar);
        botoes.add(btnEditar);
        botoes.add(btnExcluir);
        botoes.add(btnBuscarId);
        botoes.add(btnOrdenar);
        botoes.add(btnAtualizar);
        add(botoes, BorderLayout.NORTH);

        recarregar();
    }

    private void recarregar() {
        try {
            modeloTabela.setRowCount(0);
            for (Jogo j : jogoDAO.listarJogos()) {
                adicionarLinha(j);
            }
        } catch (Exception ex) {
            UiUtil.erro(this, "Erro ao listar jogos", ex);
        }
    }

    private void adicionarLinha(Jogo j) {
        modeloTabela.addRow(new Object[]{
                j.getId(), j.getTitulo(), j.getDesenvolvedora(),
                j.getClassificacaoIndicativa(), String.format("%.2f", j.getPreco()), j.getAnoLancamento()
        });
    }

    private int idSelecionado() {
        int linha = tabela.getSelectedRow();
        if (linha == -1) return -1;
        return (int) modeloTabela.getValueAt(linha, 0);
    }

    private void adicionar() {
        JogoFormDialog dialog = new JogoFormDialog(janela(), null);
        dialog.setVisible(true);
        if (!dialog.isConfirmado()) return;

        try {
            if (jogoDAO.incluirJogo(dialog.getJogo())) {
                UiUtil.info(this, "Sucesso", "Jogo incluído com sucesso (ID " + dialog.getJogo().getId() + ").");
                recarregar();
            } else {
                UiUtil.erro(this, "Erro", "Não foi possível incluir o jogo.");
            }
        } catch (Exception ex) {
            UiUtil.erro(this, "Erro ao incluir jogo", ex);
        }
    }

    private void editar() {
        int id = idSelecionado();
        if (id == -1) {
            UiUtil.erro(this, "Nenhuma seleção", "Selecione um jogo na tabela.");
            return;
        }
        try {
            Jogo jogo = jogoDAO.buscarJogo(id);
            if (jogo == null) {
                UiUtil.erro(this, "Não encontrado", "Jogo não encontrado.");
                return;
            }
            JogoFormDialog dialog = new JogoFormDialog(janela(), jogo);
            dialog.setVisible(true);
            if (!dialog.isConfirmado()) return;

            if (jogoDAO.alterarJogo(dialog.getJogo())) {
                UiUtil.info(this, "Sucesso", "Jogo alterado com sucesso.");
                recarregar();
            } else {
                UiUtil.erro(this, "Erro", "Não foi possível alterar o jogo.");
            }
        } catch (Exception ex) {
            UiUtil.erro(this, "Erro ao alterar jogo", ex);
        }
    }

    private void excluir() {
        int id = idSelecionado();
        if (id == -1) {
            UiUtil.erro(this, "Nenhuma seleção", "Selecione um jogo na tabela.");
            return;
        }
        if (!UiUtil.confirmar(this, "Confirma a exclusão do jogo " + id + "?")) return;

        try {
            if (jogoDAO.excluirJogo(id)) {
                UiUtil.info(this, "Sucesso", "Jogo excluído com sucesso.");
                recarregar();
            } else {
                UiUtil.erro(this, "Erro", "Não foi possível excluir o jogo.");
            }
        } catch (Exception ex) {
            UiUtil.erro(this, "Erro ao excluir jogo", ex);
        }
    }

    private void buscarPorId() {
        String idStr = JOptionPane.showInputDialog(this, "ID do jogo:");
        if (idStr == null || idStr.trim().isEmpty()) return;

        try {
            int id = Integer.parseInt(idStr.trim());
            Jogo j = jogoDAO.buscarJogo(id);
            modeloTabela.setRowCount(0);
            if (j == null) {
                UiUtil.info(this, "Busca", "Nenhum jogo encontrado com esse ID.");
            } else {
                adicionarLinha(j);
            }
        } catch (NumberFormatException ex) {
            UiUtil.erro(this, "ID inválido", "Digite um número inteiro.");
        } catch (Exception ex) {
            UiUtil.erro(this, "Erro ao buscar jogo", ex);
        }
    }

    private void ordenarPorPreco() {
        try {
            int totalAntes = jogoDAO.listarJogos().size();
            long inicio = System.nanoTime();
            jogoDAO.ordenarPorPreco();
            long duracaoMs = (System.nanoTime() - inicio) / 1_000_000;

            recarregar();
            UiUtil.info(this, "Ordenação concluída",
                    "jogos.db ordenado por preço (intercalação balanceada).\n" +
                    "Registros ordenados: " + totalAntes + "\n" +
                    "Tempo de execução: " + duracaoMs + " ms");
        } catch (Exception ex) {
            UiUtil.erro(this, "Erro ao ordenar jogos", ex);
        }
    }

    private Frame janela() {
        return (Frame) SwingUtilities.getWindowAncestor(this);
    }
}
