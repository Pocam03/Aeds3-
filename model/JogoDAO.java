package model;

import java.util.ArrayList;
import java.util.Comparator;

public class JogoDAO {
    private static final int TAMANHO_BLOCO_ORDENACAO = 3; // pequeno de propósito, pra forçar várias passadas de intercalação

    private Arquivo<Jogo> arqJogos;

    public JogoDAO() throws Exception {
        arqJogos = new Arquivo<>("jogos", Jogo.class.getConstructor());
    }

    public Jogo buscarJogo(int id) throws Exception {
        return arqJogos.read(id);
    }

    public boolean incluirJogo(Jogo jogo) throws Exception {
        return arqJogos.create(jogo) > 0;
    }

    public boolean alterarJogo(Jogo jogo) throws Exception {
        return arqJogos.update(jogo);
    }

    public boolean excluirJogo(int id) throws Exception {
        return arqJogos.delete(id);
    }

    public ArrayList<Jogo> listarJogos() throws Exception {
        return arqJogos.readAll();
    }

    /**
     * Ordena jogos.db pelo preço (ordenação externa por intercalação
     * balanceada). O arquivo em disco fica reescrito em ordem crescente de
     * preço, então listarJogos() passa a devolver os jogos já ordenados.
     */
    public void ordenarPorPreco() throws Exception {
        arqJogos.ordenarPorIntercalacaoBalanceada(Comparator.comparing(Jogo::getPreco), TAMANHO_BLOCO_ORDENACAO);
    }
}