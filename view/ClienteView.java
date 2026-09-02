package view;

import java.util.ArrayList;

import model.Cliente;

public class ClienteView extends ConsoleView {

    public int exibirMenuPrincipal() {
        exibirCabecalho("Início > Clientes");
        exibirLinha("\n1 - Buscar");
        exibirLinha("2 - Incluir");
        exibirLinha("3 - Alterar");
        exibirLinha("4 - Excluir");
        exibirLinha("5 - Listar todos os clientes");
        exibirLinha("0 - Voltar");
        return lerOpcao();
    }

    public void exibirOpcaoInvalidaMenu() {
        exibirOpcaoInvalida();
    }

    public int exibirMenuBusca() {
        exibirLinha("\nBuscar cliente por: ");
        exibirLinha("1 - ID");
        exibirLinha("2 - CPF");
        exibirLinha("0 - Cancelar");
        System.out.print("\nOpção: ");
        return Integer.parseInt(console.nextLine());
    }

    public int lerIdCliente(String rotulo) {
        return lerInteiro(rotulo);
    }

    public String lerCpfBusca() {
        return lerTexto("\nCPF do cliente: ");
    }

    public void exibirCliente(Cliente cliente) {
        System.out.println(cliente);
    }

    public void exibirClienteNaoEncontrado() {
        exibirLinha("Cliente não encontrado.");
    }

    public void exibirListaClientes(ArrayList<Cliente> clientes) {
        exibirLinha("\nLista de clientes");
        for (Cliente cliente : clientes) {
            System.out.println(cliente);
            exibirLinha("-------------------------");
        }
    }

    public String lerNome() {
        return lerTexto("\nNome: ");
    }

    public String lerCpfNovoCliente() {
        return lerTexto("CPF (11 dígitos): ");
    }

    public String lerDataNascimento() {
        return lerTexto("Data de nascimento (DD/MM/AAAA): ");
    }

    public String lerNovoNome() {
        return lerTexto("\nNovo nome (vazio para manter): ");
    }

    public char lerConfirmacaoExclusao() {
        return lerConfirmacao("Confirma exclusão? (S/N): ");
    }

    public void exibirMensagem(String mensagem) {
        exibirLinha(mensagem);
    }
}
