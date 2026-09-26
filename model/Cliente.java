package model;

import java.time.LocalDate;
import java.io.*;
import java.util.ArrayList;

public class Cliente implements Registro{
    private static final int TAM_TELEFONE = 11; // telefone é campo de tamanho fixo (11 dígitos)

    private int id;
    private String nome;
    private String cpf;
    private LocalDate nascimento;
    private ArrayList<String> telefones;

    public Cliente() {
        this(-1, "", "", LocalDate.now(), new ArrayList<>());
    }

    public Cliente(String n, String c, LocalDate d) {
        this(-1, n, c, d, new ArrayList<>());
    }

    public Cliente(String n, String c, LocalDate d, ArrayList<String> telefones) {
        this(-1, n, c, d, telefones);
    }

    public Cliente(int i, String n, String c, LocalDate d) {
        this(i, n, c, d, new ArrayList<>());
    }

    public Cliente(int i, String n, String c, LocalDate d, ArrayList<String> telefones) {
        this.id = i;
        this.nome = n;
        this.cpf = c;
        this.nascimento = d;
        this.telefones = telefones;
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

    public ArrayList<String> getTelefones() { return telefones; }
    public void setTelefones(ArrayList<String> telefones) { this.telefones = telefones; }
    public void adicionarTelefone(String telefone) { this.telefones.add(telefone); }
    public void removerTelefone(String telefone) { this.telefones.remove(telefone); }

 // Implementação do método toByteArray()
 public byte[] toByteArray() throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(baos);
    dos.writeInt(this.id);
    dos.writeUTF(this.nome);
    dos.writeUTF(this.cpf);
    dos.writeLong(this.nascimento.toEpochDay());

    dos.writeInt(this.telefones.size());
    for (String telefone : this.telefones) {
        dos.writeBytes(telefone); // 11 dígitos ASCII, tamanho fixo, sem prefixo
    }

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

    int qtdTelefones = dis.readInt();
    this.telefones = new ArrayList<>();
    byte[] bufferTelefone = new byte[TAM_TELEFONE];
    for (int i = 0; i < qtdTelefones; i++) {
        dis.readFully(bufferTelefone);
        this.telefones.add(new String(bufferTelefone));
    }
}
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\nID........: ").append(this.id)
          .append("\nNome......: ").append(this.nome)
          .append("\nCPF.......: ").append(this.cpf)
          .append("\nNascimento: ").append(this.nascimento)
          .append("\nTelefones.: ");

        if (this.telefones == null || this.telefones.isEmpty()) {
            sb.append("Nenhum");
        } else {
            for (int i = 0; i < this.telefones.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(this.telefones.get(i));
            }
        }

        return sb.toString();
    }
}
