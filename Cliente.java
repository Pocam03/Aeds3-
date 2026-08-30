import java.time.LocalDate;
import java.io.*;

public class Cliente implements Registro{
    private int id;
    private String nome;
    private String cpf;
    private LocalDate nascimento;

    public Cliente() {
        this(-1, "", "", LocalDate.now());
    }

    public Cliente(String n, String c, LocalDate d) {
        this(-1, n, c, d);
    }

    public Cliente(int i, String n, String c, LocalDate d) {
        this.id = i;
        this.nome = n;
        this.cpf = c;
        this.nascimento = d;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public LocalDate getNascimento() { return nascimento; }
    public void setNascimento(LocalDate nascimento) { this.nascimento = nascimento; }
 // Implementação do método toByteArray()
 public byte[] toByteArray() throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(baos);
    dos.writeInt(this.id);
    dos.writeUTF(this.nome);
    dos.writeUTF(this.cpf);
    dos.writeLong(this.nascimento.toEpochDay());
    return baos.toByteArray();
}

// Implementação do método fromByteArray()
public void fromByteArray(byte[] b) throws IOException {
    ByteArrayInputStream bais = new ByteArrayInputStream(b);
    DataInputStream dis = new DataInputStream(bais);
    this.id = dis.readInt();
    this.nome = dis.readUTF();
    this.cpf = dis.readUTF();
    this.nascimento = LocalDate.ofEpochDay(dis.readLong());
}
    @Override
    public String toString() {
        return "\nID........: " + this.id +
               "\nNome......: " + this.nome +
               "\nCPF.......: " + this.cpf +
               "\nNascimento: " + this.nascimento;
    }
}
