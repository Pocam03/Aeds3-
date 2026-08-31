import java.util.Scanner;

public class MenuCupons {
    private CupomDAO cupomDAO;
    private Scanner console = new Scanner(System.in);

    public MenuCupons() throws Exception {
        cupomDAO = new CupomDAO();
    }

    public void menu() {
        int opcao;
        do {
            System.out.println("\n\nAEDsIII");
            System.out.println("-------");
            System.out.println("> Início > Cupons");
            System.out.println("\n1 - Buscar");
            System.out.println("2 - Incluir");
            System.out.println("3 - Alterar");
            System.out.println("4 - Excluir");
            System.out.println("0 - Voltar");

            System.out.print("\nOpção: ");
            try {
                opcao = Integer.valueOf(console.nextLine());
            } catch(NumberFormatException e) {
                opcao = -1;
            }

            switch (opcao) {
                case 1:
                    buscarCupom();
                    break;
                case 2:
                    incluirCupom();
                    break;
                case 3:
                    alterarCupom();
                    break;
                case 4:
                    excluirCupom();
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opção inválida!");
                    break;
            }
        } while (opcao != 0);
    }

    private void buscarCupom() {
        System.out.print("\nID do cupom: ");
        int id = console.nextInt();
        console.nextLine();
        try {
            Cupom cupom = cupomDAO.buscarCupom(id);
            if (cupom != null) {
                System.out.println(cupom);
            } else {
                System.out.println("Cupom não encontrado.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao buscar cupom.");
        }
    }

    private void incluirCupom() {
        System.out.println("\nInclusão de cupom");

        System.out.print("\nCódigo: ");
        String codigo = console.nextLine();
        System.out.print("Valor: ");
        int valor = console.nextInt();
        console.nextLine();
        System.out.print("Porcentagem: ");
        int porcentagem = console.nextInt();
        console.nextLine();

        try {
            Cupom cupom = new Cupom(codigo, valor, porcentagem);
            if (cupomDAO.incluirCupom(cupom)) {
                System.out.println("Cupom incluído com sucesso.");
            } else {
                System.out.println("Erro ao incluir cupom.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao incluir cupom.");
        }
    }

    private void alterarCupom() {
        System.out.print("\nID do cupom a ser alterado: ");
        int id = console.nextInt();
        console.nextLine();

        try {
            Cupom cupom = cupomDAO.buscarCupom(id);
            if (cupom == null) {
                System.out.println("Cupom não encontrado.");
                return;
            }

            System.out.print("\nNovo código (vazio para manter): ");
            String codigo = console.nextLine();
            if (!codigo.isEmpty()) cupom.setCodigo(codigo);

            System.out.print("Novo valor (vazio para manter): ");
            String valorStr = console.nextLine();
            if (!valorStr.isEmpty()) cupom.setValor(Integer.parseInt(valorStr));

            System.out.print("Nova porcentagem (vazio para manter): ");
            String porcentagemStr = console.nextLine();
            if (!porcentagemStr.isEmpty()) cupom.setPorcentagem(Integer.parseInt(porcentagemStr));

            if (cupomDAO.alterarCupom(cupom)) {
                System.out.println("Cupom alterado com sucesso.");
            } else {
                System.out.println("Erro ao alterar cupom.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao alterar cupom.");
        }
    }

    private void excluirCupom() {
        System.out.print("\nID do cupom a ser excluído: ");
        int id = console.nextInt();
        console.nextLine();

        try {
            Cupom cupom = cupomDAO.buscarCupom(id);
            if (cupom == null) {
                System.out.println("Cupom não encontrado.");
                return;
            }

            System.out.print("Confirma exclusão? (S/N): ");
            char resp = console.nextLine().trim().charAt(0);
            if (resp == 'S' || resp == 's') {
                if (cupomDAO.excluirCupom(id)) {
                    System.out.println("Cupom excluído com sucesso.");
                } else {
                    System.out.println("Erro ao excluir cupom.");
                }
            }
        } catch (Exception e) {
            System.out.println("Erro ao excluir cupom.");
        }
    }
}
