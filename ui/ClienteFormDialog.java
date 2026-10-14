package ui;

import model.Cliente;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

public class ClienteFormDialog extends JDialog {
    private final JTextField campoNome = new JTextField(20);
    private final JTextField campoCpf = new JTextField(14);
    private final JTextField campoNascimento = new JTextField(10);
    private final JTextField campoNovoTelefone = new JTextField(12);
    private final DefaultListModel<String> modeloTelefones = new DefaultListModel<>();
    private final JList<String> listaTelefones = new JList<>(modeloTelefones);

    private boolean confirmado = false;
    private final Cliente cliente;

    public ClienteFormDialog(Frame pai, Cliente clienteExistente) {
        super(pai, clienteExistente == null ? "Novo Cliente" : "Editar Cliente", true);
        this.cliente = clienteExistente == null ? new Cliente() : clienteExistente;

        if (clienteExistente != null) {
            campoNome.setText(cliente.getNome());
            campoCpf.setText(cliente.getCpf());
            campoNascimento.setText(cliente.getNascimento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            for (String tel : cliente.getTelefones()) modeloTelefones.addElement(tel);

            // no console, alterarCliente só permite mudar nome e telefones
            campoCpf.setEditable(false);
            campoNascimento.setEditable(false);
        }

        montarLayout();
        pack();
        setLocationRelativeTo(pai);
    }

    private void montarLayout() {
        JPanel campos = new JPanel(new GridLayout(0, 2, 6, 6));
        campos.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        campos.add(new JLabel("Nome:"));
        campos.add(campoNome);
        campos.add(new JLabel("CPF:"));
        campos.add(campoCpf);
        campos.add(new JLabel("Nascimento (dd/MM/aaaa):"));
        campos.add(campoNascimento);

        JPanel painelTelefones = new JPanel(new BorderLayout(4, 4));
        painelTelefones.setBorder(BorderFactory.createTitledBorder("Telefones (11 dígitos)"));
        painelTelefones.add(new JScrollPane(listaTelefones), BorderLayout.CENTER);

        JPanel painelAdicionarTelefone = new JPanel(new BorderLayout(4, 4));
        painelAdicionarTelefone.add(campoNovoTelefone, BorderLayout.CENTER);
        JButton btnAdicionarTelefone = new JButton("Adicionar");
        JButton btnRemoverTelefone = new JButton("Remover selecionado");
        btnAdicionarTelefone.addActionListener(e -> adicionarTelefone());
        btnRemoverTelefone.addActionListener(e -> removerTelefoneSelecionado());
        JPanel botoesTelefone = new JPanel(new GridLayout(1, 2, 4, 4));
        botoesTelefone.add(btnAdicionarTelefone);
        botoesTelefone.add(btnRemoverTelefone);
        painelAdicionarTelefone.add(botoesTelefone, BorderLayout.SOUTH);
        painelTelefones.add(painelAdicionarTelefone, BorderLayout.SOUTH);

        JButton btnSalvar = new JButton("Salvar");
        JButton btnCancelar = new JButton("Cancelar");
        btnSalvar.addActionListener(e -> salvar());
        btnCancelar.addActionListener(e -> dispose());
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        painelBotoes.add(btnSalvar);
        painelBotoes.add(btnCancelar);

        setLayout(new BorderLayout());
        add(campos, BorderLayout.NORTH);
        add(painelTelefones, BorderLayout.CENTER);
        add(painelBotoes, BorderLayout.SOUTH);
        setMinimumSize(new Dimension(380, 350));
    }

    private void adicionarTelefone() {
        String tel = campoNovoTelefone.getText().trim();
        if (!tel.matches("\\d{11}")) {
            UiUtil.erro(this, "Telefone inválido", "O telefone precisa ter exatamente 11 dígitos numéricos.");
            return;
        }
        modeloTelefones.addElement(tel);
        campoNovoTelefone.setText("");
    }

    private void removerTelefoneSelecionado() {
        int idx = listaTelefones.getSelectedIndex();
        if (idx >= 0) modeloTelefones.remove(idx);
    }

    private void salvar() {
        String nome = campoNome.getText().trim();
        String cpf = campoCpf.getText().trim();
        String dataStr = campoNascimento.getText().trim();

        if (nome.isEmpty() || cpf.isEmpty() || dataStr.isEmpty()) {
            UiUtil.erro(this, "Campos obrigatórios", "Nome, CPF e data de nascimento são obrigatórios.");
            return;
        }

        LocalDate nascimento;
        try {
            nascimento = LocalDate.parse(dataStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (DateTimeParseException ex) {
            UiUtil.erro(this, "Data inválida", "Use o formato dd/MM/aaaa.");
            return;
        }

        ArrayList<String> telefones = new ArrayList<>();
        for (int i = 0; i < modeloTelefones.size(); i++) telefones.add(modeloTelefones.get(i));

        cliente.setNome(nome);
        cliente.setCpf(cpf);
        cliente.setNascimento(nascimento);
        cliente.setTelefones(telefones);

        confirmado = true;
        dispose();
    }

    public boolean isConfirmado() {
        return confirmado;
    }

    public Cliente getCliente() {
        return cliente;
    }
}
