package view;

public class PrincipalView extends ConsoleView {

    public int exibirMenuPerfil() {
        exibirCabecalho("Perfil de acesso");
        exibirLinha("\n1 - Admin");
        exibirLinha("2 - Cliente");
        exibirLinha("0 - Sair");
        return lerOpcao();
    }

    public int exibirMenuCliente() {
        exibirCabecalho("Início > Cliente");
        exibirLinha("\n1 - Pedidos");
        exibirLinha("0 - Sair");
        return lerOpcao();
    }

    public void exibirOpcaoInvalidaMenu() {
        exibirOpcaoInvalida();
    }

    public void exibirSaindo() {
        exibirLinha("Saindo...");
    }

    public void exibirErroFatal(Exception e) {
        System.err.println("Erro fatal no sistema:");
        e.printStackTrace();
    }
}
