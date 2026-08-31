import java.util.Scanner;

public class Principal {
    public static void main(String[] args) {
        Scanner console = new Scanner(System.in);
        int perfil;

        try {
            System.out.println("\n\nAEDsIII");
            System.out.println("-------");
            System.out.println("> Perfil de acesso");
            System.out.println("\n1 - Admin");
            System.out.println("2 - Cliente");
            System.out.println("0 - Sair");
            System.out.print("\nOpção: ");
            try {
                perfil = Integer.valueOf(console.nextLine());
            } catch (NumberFormatException e) {
                perfil = -1;
            }

            switch (perfil) {
                case 1:
                    menuAdmin(console);
                    break;
                case 2:
                    menuCliente(console);
                    break;
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida!");
                    break;
            }

        } catch (Exception e) {
            System.err.println("Erro fatal no sistema:");
            e.printStackTrace();
        } finally {
            console.close();
        }
    }

    private static void menuAdmin(Scanner console) throws Exception {
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

    private static void menuCliente(Scanner console) throws Exception {
        int opcao;
        do {
            System.out.println("\n\nAEDsIII");
            System.out.println("-------");
            System.out.println("> Início > Cliente");
            System.out.println("\n1 - Pedidos");
            System.out.println("0 - Sair");

            System.out.print("\nOpção: ");
            try {
                opcao = Integer.valueOf(console.nextLine());
            } catch (NumberFormatException e) {
                opcao = -1;
            }

            switch (opcao) {
                case 1:
                    MenuPedidos menuPedidos = new MenuPedidos();
                    menuPedidos.menu(false);
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
