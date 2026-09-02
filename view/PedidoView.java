package view;

import java.util.ArrayList;

import model.Pedido;

public class PedidoView extends ConsoleView {

    public int exibirMenuAdmin() {
        exibirCabecalho("Início > Pedidos");
        exibirLinha("\n1 - Buscar (por ID)");
        exibirLinha("2 - Listar todos os pedidos");
        exibirLinha("3 - Listar pedidos por CPF");
        exibirLinha("4 - Cancelar pedido (excluir)");
        exibirLinha("0 - Voltar");
        return lerOpcao();
    }

    public int exibirMenuCliente() {
        exibirCabecalho("Início > Pedidos");
        exibirLinha("\n1 - Buscar (por ID)");
        exibirLinha("2 - Listar meus pedidos (por CPF)");
        exibirLinha("3 - Fazer novo pedido");
        exibirLinha("4 - Alterar (jogos/cupom)");
        exibirLinha("5 - Cancelar pedido (excluir)");
        exibirLinha("0 - Voltar");
        return lerOpcao();
    }

    public void exibirOpcaoInvalidaMenu() {
        exibirOpcaoInvalida();
    }

    public String lerCpfCliente() {
        return lerTexto("\nCPF do cliente: ");
    }

    public void exibirClienteNaoEncontrado() {
        exibirLinha("Cliente não encontrado.");
    }

    public void exibirClienteNaoEncontradoPedidoCancelado() {
        exibirLinha("Cliente não encontrado. Pedido cancelado.");
    }

    public void exibirNenhumPedidoDoCliente() {
        exibirLinha("Nenhum pedido encontrado para este cliente.");
    }

    public void exibirPedidosDoCliente(ArrayList<Pedido> pedidos) {
        exibirLinha("\nPedidos do cliente");
        for (Pedido pedido : pedidos) {
            System.out.println(pedido);
            exibirLinha("-------------------------");
        }
    }

    public int lerIdPedido(String rotulo) {
        return lerInteiro(rotulo);
    }

    public void exibirPedido(Pedido pedido) {
        System.out.println(pedido);
    }

    public void exibirPedidoNaoEncontrado() {
        exibirLinha("Pedido não encontrado.");
    }

    public int lerIdJogoAdicionar() {
        return lerInteiro("ID do jogo a adicionar: ");
    }

    public void exibirJogoNaoEncontrado() {
        exibirLinha("Jogo não encontrado. Tente outro ID.");
    }

    public void exibirJogoAdicionado(String titulo) {
        exibirLinha("Adicionado: " + titulo);
    }

    public char lerAdicionarOutroJogo() {
        return lerConfirmacao("Adicionar outro jogo? (S/N): ");
    }

    public void exibirNenhumJogoAdicionado() {
        exibirLinha("Nenhum jogo adicionado. Pedido cancelado.");
    }

    public char lerAplicarCupom() {
        return lerConfirmacao("Deseja aplicar um cupom? (S/N): ");
    }

    public String lerCodigoCupom() {
        return lerTexto("Código do cupom: ");
    }

    public void exibirCupomNaoEncontradoSemDesconto() {
        exibirLinha("Cupom não encontrado. Pedido seguirá sem desconto.");
    }

    public void exibirPedidoIncluidoComSucesso(float total) {
        System.out.printf("Pedido incluído com sucesso. Total: R$ %.2f%n", total);
    }

    public void exibirJogosAtuais(ArrayList<Integer> idJogos) {
        exibirLinha("\nJogos atuais (IDs): " + idJogos);
    }

    public char lerAdicionarNovoJogo() {
        return lerConfirmacao("Adicionar novo jogo ao pedido? (S/N): ");
    }

    public char lerRemoverJogo() {
        return lerConfirmacao("Remover algum jogo do pedido? (S/N): ");
    }

    public int lerIdJogoRemover() {
        return lerInteiro("ID do jogo a remover: ");
    }

    public void exibirJogoRemovido() {
        exibirLinha("Jogo removido (se estava no pedido).");
    }

    public char lerAlterarCupom() {
        return lerConfirmacao("Alterar cupom do pedido? (S/N): ");
    }

    public String lerNovoCodigoCupom() {
        return lerTexto("Novo código de cupom (vazio para remover cupom): ");
    }

    public void exibirCupomNaoEncontradoNaoAlterado() {
        exibirLinha("Cupom não encontrado. Cupom não foi alterado.");
    }

    public void exibirPedidoAlteradoComSucesso(float valorFinal) {
        exibirLinha("Pedido alterado com sucesso. Novo valor final: " + valorFinal);
    }

    public char lerConfirmacaoExclusao() {
        return lerConfirmacao("Confirma exclusão? (S/N): ");
    }

    public void exibirListaPedidos(ArrayList<Pedido> pedidos) {
        exibirLinha("\nLista de pedidos");
        for (Pedido pedido : pedidos) {
            System.out.println(pedido);
            exibirLinha("-------------------------");
        }
    }

    public void exibirMensagem(String mensagem) {
        exibirLinha(mensagem);
    }
}
