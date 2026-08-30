
public class JogoDAO {
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
}