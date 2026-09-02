package controller;

import view.PrincipalView;

public class Principal {
    public static void main(String[] args) {
        PrincipalView view = new PrincipalView();

        try {
            int perfil = view.exibirMenuPerfil();

            switch (perfil) {
                case 1:
                    MenuAdmin menuAdmin = new MenuAdmin();
                    menuAdmin.menu();
                    break;
                case 2:
                    menuCliente(view);
                    break;
                case 0:
                    view.exibirSaindo();
                    break;
                default:
                    view.exibirOpcaoInvalidaMenu();
                    break;
            }
        } catch (Exception e) {
            view.exibirErroFatal(e);
        } finally {
            view.fechar();
        }
    }

    private static void menuCliente(PrincipalView view) throws Exception {
        int opcao;
        do {
            opcao = view.exibirMenuCliente();

            switch (opcao) {
                case 1:
                    MenuPedidos menuPedidos = new MenuPedidos();
                    menuPedidos.menu(false);
                    break;
                case 0:
                    view.exibirSaindo();
                    break;
                default:
                    view.exibirOpcaoInvalidaMenu();
                    break;
            }
        } while (opcao != 0);
    }
}
