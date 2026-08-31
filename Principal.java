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
                    MenuAdmin menuAdmin = new MenuAdmin();
                    menuAdmin.menu();
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
