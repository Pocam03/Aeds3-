package view;

import java.util.Scanner;

public abstract class ConsoleView {
    protected Scanner console = new Scanner(System.in);

    protected void exibirCabecalho(String caminho) {
        System.out.println("\n\nAEDsIII");
        System.out.println("-------");
        System.out.println("> " + caminho);
    }

    protected void exibirLinha(String texto) {
        System.out.println(texto);
    }

    protected void exibirOpcaoInvalida() {
        System.out.println("Opção inválida!");
    }

    protected int lerOpcao() {
        System.out.print("\nOpção: ");
        try {
            return Integer.valueOf(console.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    protected String lerTexto(String rotulo) {
        System.out.print(rotulo);
        return console.nextLine();
    }

    protected int lerInteiro(String rotulo) {
        System.out.print(rotulo);
        int valor = console.nextInt();
        console.nextLine();
        return valor;
    }

    protected char lerConfirmacao(String rotulo) {
        System.out.print(rotulo);
        return console.nextLine().trim().charAt(0);
    }

    public void fechar() {
        console.close();
    }
}
