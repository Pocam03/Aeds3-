import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Scanner;

public class MenuPedidos {
    private PedidoDAO pedidoDAO;
    private ClienteDAO clienteDAO;
    private JogoDAO jogoDAO;
    private CupomDAO cupomDAO;
    private Scanner console = new Scanner(System.in);

    public MenuPedidos() throws Exception {
        pedidoDAO = new PedidoDAO();
        clienteDAO = new ClienteDAO();
        jogoDAO = new JogoDAO();
        cupomDAO = new CupomDAO();
    }

    public void menu() {
        int opcao;
        do {
            System.out.println("\n\nAEDsIII");
            System.out.println("-------");
            System.out.println("> Início > Pedidos");
            System.out.println("\n1 - Buscar");
            System.out.println("2 - Incluir");
            System.out.println("3 - Alterar (jogos/cupom)");
            System.out.println("4 - Excluir");
            System.out.println("5 - Listar todos os pedidos");
            System.out.println("0 - Voltar");

            System.out.print("\nOpção: ");
            try {
                opcao = Integer.valueOf(console.nextLine());
            } catch (NumberFormatException e) {
                opcao = -1;
            }

            switch (opcao) {
                case 1:
                    buscarPedido();
                    break;
                case 2:
                    incluirPedido();
                    break;
                case 3:
                    alterarPedido();
                    break;
                case 4:
                    excluirPedido();
                    break;
                case 5:
                    listarPedidos();
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opção inválida!");
                    break;
            }
        } while (opcao != 0);
    }

    private void buscarPedido() {
        System.out.print("\nID do pedido: ");
        int id = console.nextInt();
        console.nextLine();
        try {
            Pedido pedido = pedidoDAO.buscarPedido(id);
            if (pedido != null) {
                System.out.println(pedido);
            } else {
                System.out.println("Pedido não encontrado.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao buscar pedido.");
        }
    }

    private void incluirPedido() {
        System.out.println("\nInclusão de pedido");

        try {
            System.out.print("\nID do cliente: ");
            int idCliente = console.nextInt();
            console.nextLine();

            Cliente cliente = clienteDAO.buscarCliente(idCliente);
            if (cliente == null) {
                System.out.println("Cliente não encontrado. Pedido cancelado.");
                return;
            }

            ArrayList<Integer> idJogos = new ArrayList<>();
            boolean adicionando = true;
            while (adicionando) {
                System.out.print("ID do jogo a adicionar: ");
                int idJogo = console.nextInt();
                console.nextLine();

                Jogo jogo = jogoDAO.buscarJogo(idJogo);
                if (jogo == null) {
                    System.out.println("Jogo não encontrado. Tente outro ID.");
                } else {
                    idJogos.add(idJogo);
                    System.out.println("Adicionado: " + jogo.getTitulo());
                }

                System.out.print("Adicionar outro jogo? (S/N): ");
                char resp = console.nextLine().trim().charAt(0);
                adicionando = (resp == 'S' || resp == 's');
            }

            if (idJogos.isEmpty()) {
                System.out.println("Nenhum jogo adicionado. Pedido cancelado.");
                return;
            }

            int idCupom = -1;
            System.out.print("Deseja aplicar um cupom? (S/N): ");
            char respCupom = console.nextLine().trim().charAt(0);
            if (respCupom == 'S' || respCupom == 's') {
                System.out.print("ID do cupom: ");
                idCupom = console.nextInt();
                console.nextLine();

                Cupom cupom = cupomDAO.buscarCupom(idCupom);
                if (cupom == null) {
                    System.out.println("Cupom não encontrado. Pedido seguirá sem desconto.");
                    idCupom = -1;
                }
            }

            if (pedidoDAO.incluirPedido(idCliente, idJogos, idCupom)) {
                System.out.println("Pedido incluído com sucesso.");
            } else {
                System.out.println("Erro ao incluir pedido.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao incluir pedido.");
        }
    }

    private void alterarPedido() {
        System.out.print("\nID do pedido a ser alterado: ");
        int id = console.nextInt();
        console.nextLine();

        try {
            Pedido pedido = pedidoDAO.buscarPedido(id);
            if (pedido == null) {
                System.out.println("Pedido não encontrado.");
                return;
            }

            System.out.println("\nJogos atuais (IDs): " + pedido.getIdJogos());
            System.out.print("Adicionar novo jogo ao pedido? (S/N): ");
            char respAdd = console.nextLine().trim().charAt(0);
            if (respAdd == 'S' || respAdd == 's') {
                System.out.print("ID do jogo a adicionar: ");
                int idJogo = console.nextInt();
                console.nextLine();

                Jogo jogo = jogoDAO.buscarJogo(idJogo);
                if (jogo != null) {
                    pedido.adicionarJogo(idJogo);
                    System.out.println("Adicionado: " + jogo.getTitulo());
                } else {
                    System.out.println("Jogo não encontrado.");
                }
            }

            System.out.print("Remover algum jogo do pedido? (S/N): ");
            char respRem = console.nextLine().trim().charAt(0);
            if (respRem == 'S' || respRem == 's') {
                System.out.print("ID do jogo a remover: ");
                int idJogo = console.nextInt();
                console.nextLine();
                pedido.removerJogo(idJogo);
                System.out.println("Jogo removido (se estava no pedido).");
            }

            System.out.print("Alterar cupom do pedido? (S/N): ");
            char respCupom = console.nextLine().trim().charAt(0);
            if (respCupom == 'S' || respCupom == 's') {
                System.out.print("Novo ID de cupom (-1 para remover cupom): ");
                int idCupom = console.nextInt();
                console.nextLine();

                if (idCupom == -1) {
                    pedido.setIdCupom(-1);
                } else {
                    Cupom cupom = cupomDAO.buscarCupom(idCupom);
                    if (cupom != null) {
                        pedido.setIdCupom(idCupom);
                    } else {
                        System.out.println("Cupom não encontrado. Cupom não foi alterado.");
                    }
                }
            }

            if (pedidoDAO.alterarPedido(pedido)) {
                System.out.println("Pedido alterado com sucesso. Novo valor final: " + pedido.getValorFinal());
            } else {
                System.out.println("Erro ao alterar pedido.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao alterar pedido.");
        }
    }

    private void excluirPedido() {
        System.out.print("\nID do pedido a ser excluído: ");
        int id = console.nextInt();
        console.nextLine();

        try {
            Pedido pedido = pedidoDAO.buscarPedido(id);
            if (pedido == null) {
                System.out.println("Pedido não encontrado.");
                return;
            }

            System.out.print("Confirma exclusão? (S/N): ");
            char resp = console.nextLine().trim().charAt(0);
            if (resp == 'S' || resp == 's') {
                if (pedidoDAO.excluirPedido(id)) {
                    System.out.println("Pedido excluído com sucesso.");
                } else {
                    System.out.println("Erro ao excluir pedido.");
                }
            }
        } catch (Exception e) {
            System.out.println("Erro ao excluir pedido.");
        }
    }

    private void listarPedidos() {
        try {
            ArrayList<Pedido> pedidos = pedidoDAO.listarPedidos();

            if (pedidos.isEmpty()) {
                System.out.println("\nNenhum pedido cadastrado.");
                return;
            }

            System.out.println("\nLista de pedidos");

            for (Pedido pedido : pedidos) {
                System.out.println(pedido);
                System.out.println("-------------------------");
            }
        } catch (Exception e) {
            System.out.println("Erro ao listar pedidos.");
        }
    }
}