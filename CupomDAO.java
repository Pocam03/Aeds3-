import java.util.ArrayList;
public class CupomDAO {
    private Arquivo<Cupom> arqCupons;

    public CupomDAO() throws Exception {
        arqCupons = new Arquivo<>("cupons", Cupom.class.getConstructor());
    }

    public Cupom buscarCupom(int id) throws Exception {
        return arqCupons.read(id);
    }

    public boolean incluirCupom(Cupom cupom) throws Exception {
        return arqCupons.create(cupom) > 0;
    }

    public boolean alterarCupom(Cupom cupom) throws Exception {
        return arqCupons.update(cupom);
    }

    public boolean excluirCupom(int id) throws Exception {
        return arqCupons.delete(id);
    }

    public ArrayList<Cupom> listarCupons() throws Exception {
        return arqCupons.readAll();
    }
}
