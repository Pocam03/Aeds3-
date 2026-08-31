import java.util.Scanner;

public class MenuAdmin {
    private Scanner console = new Scanner(System.in);

    public void menu() throws Exception {
        int opcao;
        do {
            System.out.println("\n\nAEDsIII");
            System.out.println("-------");
            System.out.println("> Início > Admin");
            System.out.println("\n1 - Clientes");
            System.out.println("2 - Jogos");
            System.out.println("3 - Cupons");
            System.out.println("4 - Pedidos");
            System.out.println("0 - Sair");

            System.out.print("\nOpção: ");
            try {
                opcao = Integer.valueOf(console.nextLine());
            } catch (NumberFormatException e) {
                opcao = -1;
            }

            switch (opcao) {
                case 1:
                    MenuClientes menuClientes = new MenuClientes();
                    menuClientes.menu();
                    break;
                case 2:
                    MenuJogos menuJogos = new MenuJogos();
                    menuJogos.menu();
                    break;
                case 3:
                    MenuCupons menuCupons = new MenuCupons();
                    menuCupons.menu();
                    break;
                case 4:
                    MenuPedidos menuPedidos = new MenuPedidos();
                    menuPedidos.menu(true);
                    break;
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida!");
                    break;
            }
        } while (opcao != 0);
    }
}
