package ui;

import model.Cupom;

import javax.swing.*;
import java.awt.*;

public class CupomFormDialog extends JDialog {
    private final JTextField campoCodigo = new JTextField(15);
    private final JTextField campoValor = new JTextField(8);
    private final JTextField campoPorcentagem = new JTextField(8);

    private boolean confirmado = false;
    private final Cupom cupom;

    public CupomFormDialog(Frame pai, Cupom cupomExistente) {
        super(pai, cupomExistente == null ? "Novo Cupom" : "Editar Cupom", true);
        this.cupom = cupomExistente == null ? new Cupom() : cupomExistente;

        if (cupomExistente != null) {
            campoCodigo.setText(cupom.getCodigo());
            campoValor.setText(String.valueOf(cupom.getValor()));
            campoPorcentagem.setText(String.valueOf(cupom.getPorcentagem()));
        }

        montarLayout();
        pack();
        setLocationRelativeTo(pai);
    }

    private void montarLayout() {
        JPanel campos = new JPanel(new GridLayout(0, 2, 6, 6));
        campos.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        campos.add(new JLabel("Código:"));
        campos.add(campoCodigo);
        campos.add(new JLabel("Valor (desconto fixo, R$):"));
        campos.add(campoValor);
        campos.add(new JLabel("Porcentagem (%):"));
        campos.add(campoPorcentagem);

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
        String codigo = campoCodigo.getText().trim();
        if (codigo.isEmpty()) {
            UiUtil.erro(this, "Campo obrigatório", "Código é obrigatório.");
            return;
        }

        int valor, porcentagem;
        try {
            valor = Integer.parseInt(campoValor.getText().trim());
            porcentagem = Integer.parseInt(campoPorcentagem.getText().trim());
        } catch (NumberFormatException ex) {
            UiUtil.erro(this, "Valor inválido", "Valor e porcentagem precisam ser números inteiros.");
            return;
        }

        cupom.setCodigo(codigo);
        cupom.setValor(valor);
        cupom.setPorcentagem(porcentagem);

        confirmado = true;
        dispose();
    }

    public boolean isConfirmado() {
        return confirmado;
    }

    public Cupom getCupom() {
        return cupom;
    }
}
