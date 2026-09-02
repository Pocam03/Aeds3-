package view;

import java.util.ArrayList;

import model.Jogo;

public class JogoView extends ConsoleView {

    public int exibirMenuPrincipal() {
        exibirCabecalho("Início > Jogos");
        exibirLinha("\n1 - Buscar");
        exibirLinha("2 - Incluir");
        exibirLinha("3 - Alterar");
        exibirLinha("4 - Excluir");
        exibirLinha("5 - Listar todos os jogos");
        exibirLinha("0 - Voltar");
        return lerOpcao();
    }

    public void exibirOpcaoInvalidaMenu() {
        exibirOpcaoInvalida();
    }

    public int lerIdJogo(String rotulo) {
        return lerInteiro(rotulo);
    }

    public void exibirJogo(Jogo jogo) {
        System.out.println(jogo);
    }

    public void exibirJogoNaoEncontrado() {
        exibirLinha("Jogo não encontrado.");
    }

    public void exibirListaJogos(ArrayList<Jogo> jogos) {
        exibirLinha("\nLista de jogos");
        for (Jogo jogo : jogos) {
            System.out.println(jogo);
            exibirLinha("-------------------------");
        }
    }

    public String lerTitulo() {
        return lerTexto("\nTítulo: ");
    }

    public String lerDesenvolvedora() {
        return lerTexto("Desenvolvedora: ");
    }

    public int lerClassificacao() {
        return lerInteiro("Classificação indicativa (0, 10, 12, 14, 16, 18): ");
    }

    public float lerPreco() {
        System.out.print("Preço: ");
        return console.nextFloat();
    }

    public int lerAnoLancamento() {
        return lerInteiro("Ano de lançamento: ");
    }

    public String lerNovoTitulo() {
        return lerTexto("\nNovo título (vazio para manter): ");
    }

    public String lerNovaDesenvolvedora() {
        return lerTexto("Nova desenvolvedora (vazio para manter): ");
    }

    public String lerNovaClassificacao() {
        return lerTexto("Nova classificação indicativa (vazio para manter): ");
    }

    public String lerNovoPreco() {
        return lerTexto("Novo preço (vazio para manter): ");
    }

    public String lerNovoAno() {
        return lerTexto("Novo ano de lançamento (vazio para manter): ");
    }

    public char lerConfirmacaoExclusao() {
        return lerConfirmacao("Confirma exclusão? (S/N): ");
    }

    public void exibirMensagem(String mensagem) {
        exibirLinha(mensagem);
    }
}
