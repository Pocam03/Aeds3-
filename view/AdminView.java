package view;

public class AdminView extends ConsoleView {

    public int exibirMenuPrincipal() {
        exibirCabecalho("Início > Admin");
        exibirLinha("\n1 - Clientes");
        exibirLinha("2 - Jogos");
        exibirLinha("3 - Cupons");
        exibirLinha("4 - Pedidos");
        exibirLinha("0 - Sair");
        return lerOpcao();
    }

    public void exibirOpcaoInvalidaMenu() {
        exibirOpcaoInvalida();
    }

    public void exibirSaindo() {
        exibirLinha("Saindo...");
    }
}
