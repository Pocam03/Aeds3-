import java.io.*;

public class Cupom implements Registro {
    private int id;
    private String codigo;
    private int valor;
    private int porcentagem;

    public Cupom() {
        this(-1, "", 0, 0);
    }

    public Cupom(String codigo, int valor, int porcentagem) {
        this(-1, codigo, valor, porcentagem);
    }

    public Cupom(int id, String codigo, int valor, int porcentagem) {
        this.id = id;
        this.codigo = codigo;
        this.valor = valor;
        this.porcentagem = porcentagem;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public int getValor() { return valor; }
    public void setValor(int valor) { this.valor = valor; }
    public int getPorcentagem() { return porcentagem; }
    public void setPorcentagem(int porcentagem) { this.porcentagem = porcentagem; }

    // Implementação do método toByteArray()
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(this.id);
        dos.writeUTF(this.codigo);
        dos.writeInt(this.valor);
        dos.writeInt(this.porcentagem);
        return baos.toByteArray();
    }

    // Implementação do método fromByteArray()
    public void fromByteArray(byte[] b) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        DataInputStream dis = new DataInputStream(bais);
        this.id = dis.readInt();
        this.codigo = dis.readUTF();
        this.valor = dis.readInt();
        this.porcentagem = dis.readInt();
    }

    @Override
    public String toString() {
        return "\nID...........: " + this.id +
               "\nCódigo.......: " + this.codigo +
               "\nValor........: " + this.valor +
               "\nPorcentagem..: " + this.porcentagem + "%";
    }
}
