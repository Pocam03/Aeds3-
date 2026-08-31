import java.util.ArrayList;
import java.util.Scanner;

public class MenuJogos {
    private JogoDAO jogoDAO;
    private Scanner console = new Scanner(System.in);

    public MenuJogos() throws Exception {
        jogoDAO = new JogoDAO();
    }

    public void menu() {
        int opcao;
        do {
            System.out.println("\n\nAEDsIII");
            System.out.println("-------");
            System.out.println("> Início > Jogos");
            System.out.println("\n1 - Buscar");
            System.out.println("2 - Incluir");
            System.out.println("3 - Alterar");
            System.out.println("4 - Excluir");
            System.out.println("5 - Listar todos os jogos");
            System.out.println("0 - Voltar");

            System.out.print("\nOpção: ");
            try {
                opcao = Integer.valueOf(console.nextLine());
            } catch (NumberFormatException e) {
                opcao = -1;
            }

            switch (opcao) {
                case 1:
                    buscarJogo();
                    break;
                case 2:
                    incluirJogo();
                    break;
                case 3:
                    alterarJogo();
                    break;
                case 4:
                    excluirJogo();
                    break;
                case 5:
                    listarJogos();
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opção inválida!");
                    break;
            }
        } while (opcao != 0);
    }

    private void buscarJogo() {
        System.out.print("\nID do jogo: ");
        int id = console.nextInt();
        console.nextLine();
        try {
            Jogo jogo = jogoDAO.buscarJogo(id);
            if (jogo != null) {
                System.out.println(jogo);
            } else {
                System.out.println("Jogo não encontrado.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao buscar jogo.");
        }
    }

    private void incluirJogo() {
        System.out.println("\nInclusão de jogo");

        System.out.print("\nTítulo: ");
        String titulo = console.nextLine();
        System.out.print("Desenvolvedora: ");
        String desenvolvedora = console.nextLine();
        System.out.print("Classificação indicativa (0, 10, 12, 14, 16, 18): ");
        int classificacao = console.nextInt();
        System.out.print("Preço: ");
        float preco = console.nextFloat();
        System.out.print("Ano de lançamento: ");
        int ano = console.nextInt();
        console.nextLine();

        try {
            Jogo jogo = new Jogo(titulo, desenvolvedora, classificacao, preco, ano);
            if (jogoDAO.incluirJogo(jogo)) {
                System.out.println("Jogo incluído com sucesso.");
            } else {
                System.out.println("Erro ao incluir jogo.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao incluir jogo.");
        }
    }

    private void alterarJogo() {
        System.out.print("\nID do jogo a ser alterado: ");
        int id = console.nextInt();
        console.nextLine();

        try {
            Jogo jogo = jogoDAO.buscarJogo(id);
            if (jogo == null) {
                System.out.println("Jogo não encontrado.");
                return;
            }

            System.out.print("\nNovo título (vazio para manter): ");
            String titulo = console.nextLine();
            if (!titulo.isEmpty()) jogo.setTitulo(titulo);

            System.out.print("Nova desenvolvedora (vazio para manter): ");
            String desenvolvedora = console.nextLine();
            if (!desenvolvedora.isEmpty()) jogo.setDesenvolvedora(desenvolvedora);

            System.out.print("Nova classificação indicativa (vazio para manter): ");
            String classificacaoStr = console.nextLine();
            if (!classificacaoStr.isEmpty()) jogo.setClassificacaoIndicativa(Integer.parseInt(classificacaoStr));

            System.out.print("Novo preço (vazio para manter): ");
            String precoStr = console.nextLine();
            if (!precoStr.isEmpty()) jogo.setPreco(Float.parseFloat(precoStr));

            System.out.print("Novo ano de lançamento (vazio para manter): ");
            String anoStr = console.nextLine();
            if (!anoStr.isEmpty()) jogo.setAnoLancamento(Integer.parseInt(anoStr));

            if (jogoDAO.alterarJogo(jogo)) {
                System.out.println("Jogo alterado com sucesso.");
            } else {
                System.out.println("Erro ao alterar jogo.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao alterar jogo.");
        }
    }

    private void excluirJogo() {
        System.out.print("\nID do jogo a ser excluído: ");
        int id = console.nextInt();
        console.nextLine();

        try {
            Jogo jogo = jogoDAO.buscarJogo(id);
            if (jogo == null) {
                System.out.println("Jogo não encontrado.");
                return;
            }

            System.out.print("Confirma exclusão? (S/N): ");
            char resp = console.nextLine().trim().charAt(0);
            if (resp == 'S' || resp == 's') {
                if (jogoDAO.excluirJogo(id)) {
                    System.out.println("Jogo excluído com sucesso.");
                } else {
                    System.out.println("Erro ao excluir jogo.");
                }
            }
        } catch (Exception e) {
            System.out.println("Erro ao excluir jogo.");
        }
    }

    private void listarJogos() {
        try {
            ArrayList<Jogo> jogos =
                jogoDAO.listarJogos();

            if (jogos.isEmpty()) {
                System.out.println("\nNenhum jogo cadastrado.");
                return;
            }

            System.out.println("\nLista de jogos");

            for (Jogo jogo : jogos) {
                System.out.println(jogo);
                System.out.println("-------------------------");
            }

        } catch (Exception e) {
            System.out.println("Erro ao listar jogos.");
            e.printStackTrace();
        }
    }   
}