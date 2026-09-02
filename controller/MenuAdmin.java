package controller;

import view.AdminView;

public class MenuAdmin {
    private AdminView view = new AdminView();

    public void menu() throws Exception {
        int opcao;
        do {
            opcao = view.exibirMenuPrincipal();

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
                    view.exibirSaindo();
                    break;
                default:
                    view.exibirOpcaoInvalidaMenu();
                    break;
            }
        } while (opcao != 0);
    }
}
