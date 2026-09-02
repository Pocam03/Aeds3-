package controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import model.Cliente;
import model.ClienteDAO;
import view.ClienteView;

public class MenuClientes {
    private ClienteDAO clienteDAO;
    private ClienteView view = new ClienteView();

    public MenuClientes() throws Exception {
        clienteDAO = new ClienteDAO();
    }

    public void menu() {
        int opcao;
        do {
            opcao = view.exibirMenuPrincipal();

            switch (opcao) {
                case 1:
                    buscarCliente();
                    break;
                case 2:
                    incluirCliente();
                    break;
                case 3:
                    alterarCliente();
                    break;
                case 4:
                    excluirCliente();
                    break;
                case 5:
                    listarClientes();
                    break;
                case 0:
                    break;
                default:
                    view.exibirOpcaoInvalidaMenu();
                    break;
            }
        } while (opcao != 0);
    }

    private void buscarCliente() {
        int opcao;
        do {
            opcao = view.exibirMenuBusca();
            switch (opcao) {
                case 1:
                    buscarClientePorId();
                    break;
                case 2:
                    buscarClientePorCpf();
                    break;
                case 0:
                    break;
                default:
                    view.exibirOpcaoInvalidaMenu();
                    break;
            }
        } while (opcao != 1 && opcao != 2);
    }

    private void buscarClientePorId() {
        int id = view.lerIdCliente("\nID do cliente: ");
        try {
            Cliente cliente = clienteDAO.buscarCliente(id);
            if (cliente != null) {
                view.exibirCliente(cliente);
            } else {
                view.exibirClienteNaoEncontrado();
            }
        } catch (Exception e) {
            view.exibirMensagem("Erro ao buscar cliente.");
        }
    }

    private void buscarClientePorCpf() {
        String cpf = view.lerCpfBusca();
        try {
            Cliente cliente = clienteDAO.buscarClientePorCpf(cpf);
            if (cliente != null) {
                view.exibirCliente(cliente);
            } else {
                view.exibirClienteNaoEncontrado();
            }
        } catch (Exception e) {
            view.exibirMensagem("Erro ao buscar cliente.");
        }
    }

    private void incluirCliente() {
        view.exibirMensagem("\nInclusão de cliente");

        String nome = view.lerNome();
        String cpf = view.lerCpfNovoCliente();
        String dataStr = view.lerDataNascimento();

        if (nome.isEmpty() || cpf.isEmpty() || dataStr.isEmpty()) {
            view.exibirMensagem("Não foi possível registrar o cliente.");
            return;
        }

        try {
            LocalDate nascimento = LocalDate.parse(dataStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            Cliente cliente = new Cliente(nome, cpf, nascimento);
            if (clienteDAO.incluirCliente(cliente)) {
                view.exibirCliente(cliente);
                view.exibirMensagem("Cliente incluído com sucesso.");
            } else {
                view.exibirMensagem("Não foi possível registrar o cliente.");
            }
        } catch (Exception e) {
            view.exibirMensagem("Não foi possível registrar o cliente.");
        }
    }

    private void alterarCliente() {
        int id = view.lerIdCliente("\nID do cliente a ser alterado: ");

        try {
            Cliente cliente = clienteDAO.buscarCliente(id);
            if (cliente == null) {
                view.exibirClienteNaoEncontrado();
                return;
            }

            String nome = view.lerNovoNome();
            if (!nome.isEmpty()) cliente.setNome(nome);

            if (clienteDAO.alterarCliente(cliente)) {
                view.exibirMensagem("Cliente alterado com sucesso.");
            } else {
                view.exibirMensagem("Erro ao alterar cliente.");
            }
        } catch (Exception e) {
            view.exibirMensagem("Erro ao alterar cliente.");
        }
    }

    private void excluirCliente() {
        int id = view.lerIdCliente("\nID do cliente a ser excluído: ");

        try {
            Cliente cliente = clienteDAO.buscarCliente(id);
            if (cliente == null) {
                view.exibirClienteNaoEncontrado();
                return;
            }

            char resp = view.lerConfirmacaoExclusao();
            if (resp == 'S' || resp == 's') {
                if (clienteDAO.excluirCliente(id)) {
                    view.exibirMensagem("Cliente excluído com sucesso.");
                } else {
                    view.exibirMensagem("Erro ao excluir cliente.");
                }
            }
        } catch (Exception e) {
            view.exibirMensagem("Erro ao excluir cliente.");
        }
    }

    private void listarClientes() {
        try {
            ArrayList<Cliente> clientes = clienteDAO.listarClientes();

            if (clientes.isEmpty()) {
                view.exibirMensagem("\nNenhum cliente cadastrado.");
                return;
            }

            view.exibirListaClientes(clientes);
        } catch (Exception e) {
            view.exibirMensagem("Erro ao listar clientes.");
        }
    }
}
