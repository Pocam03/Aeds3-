package ui;

import model.Jogo;

import javax.swing.*;
import java.awt.*;

public class JogoFormDialog extends JDialog {
    private final JTextField campoTitulo = new JTextField(20);
    private final JTextField campoDesenvolvedora = new JTextField(20);
    private final JComboBox<Integer> campoClassificacao = new JComboBox<>(new Integer[]{0, 10, 12, 14, 16, 18});
    private final JTextField campoPreco = new JTextField(10);
    private final JTextField campoAno = new JTextField(6);

    private boolean confirmado = false;
    private final Jogo jogo;

    public JogoFormDialog(Frame pai, Jogo jogoExistente) {
        super(pai, jogoExistente == null ? "Novo Jogo" : "Editar Jogo", true);
        this.jogo = jogoExistente == null ? new Jogo() : jogoExistente;

        if (jogoExistente != null) {
            campoTitulo.setText(jogo.getTitulo());
            campoDesenvolvedora.setText(jogo.getDesenvolvedora());
            campoClassificacao.setSelectedItem(jogo.getClassificacaoIndicativa());
            campoPreco.setText(String.valueOf(jogo.getPreco()));
            campoAno.setText(String.valueOf(jogo.getAnoLancamento()));
        }

        montarLayout();
        pack();
        setLocationRelativeTo(pai);
    }

    private void montarLayout() {
        JPanel campos = new JPanel(new GridLayout(0, 2, 6, 6));
        campos.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        campos.add(new JLabel("Título:"));
        campos.add(campoTitulo);
        campos.add(new JLabel("Desenvolvedora:"));
        campos.add(campoDesenvolvedora);
        campos.add(new JLabel("Classificação indicativa:"));
        campos.add(campoClassificacao);
        campos.add(new JLabel("Preço:"));
        campos.add(campoPreco);
        campos.add(new JLabel("Ano de lançamento:"));
        campos.add(campoAno);

        JButton btnSalvar = new JButton("Salvar");
        JButton btnCancelar = new JButton("Cancelar");
        btnSalvar.addActionListener(e -> salvar());
        btnCancelar.addActionListener(e -> dispose());
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        painelBotoes.add(btnSalvar);
        painelBotoes.add(btnCancelar);

        setLayout(new BorderLayout());
        add(campos, BorderLayout.CENTER);
        add(painelBotoes, BorderLayout.SOUTH);
    }

    private void salvar() {
        String titulo = campoTitulo.getText().trim();
        String desenvolvedora = campoDesenvolvedora.getText().trim();

        if (titulo.isEmpty() || desenvolvedora.isEmpty()) {
            UiUtil.erro(this, "Campos obrigatórios", "Título e desenvolvedora são obrigatórios.");
            return;
        }

        float preco;
        int ano;
        try {
            preco = Float.parseFloat(campoPreco.getText().trim().replace(",", "."));
            ano = Integer.parseInt(campoAno.getText().trim());
        } catch (NumberFormatException ex) {
            UiUtil.erro(this, "Valor inválido", "Preço e ano precisam ser números válidos.");
            return;
        }

        jogo.setTitulo(titulo);
        jogo.setDesenvolvedora(desenvolvedora);
        jogo.setClassificacaoIndicativa((Integer) campoClassificacao.getSelectedItem());
        jogo.setPreco(preco);
        jogo.setAnoLancamento(ano);

        confirmado = true;
        dispose();
    }

    public boolean isConfirmado() {
        return confirmado;
    }

    public Jogo getJogo() {
        return jogo;
    }
}
