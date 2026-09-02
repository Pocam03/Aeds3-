package model;

import java.io.*;

public class Jogo implements Registro {
    private int id;
    private String titulo;
    private String desenvolvedora;
    private int classificacaoIndicativa;
    private float preco;
    private int anoLancamento;

    public Jogo() {
    }

    public Jogo(String t, String d, int c, float p, int a) {
        this(-1, t, d, c, p, a);
    }

    public Jogo(int i, String t, String d, int c, float p, int a) {
        this.id = i;
        this.titulo = t;
        this.desenvolvedora = d;
        this.classificacaoIndicativa = c;
        this.preco = p;
        this.anoLancamento = a;
    }

    // Getters e Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDesenvolvedora() {
        return desenvolvedora;
    }

    public void setDesenvolvedora(String desenvolvedora) {
        this.desenvolvedora = desenvolvedora;
    }

    public int getClassificacaoIndicativa() {
        return classificacaoIndicativa;
    }

    public void setClassificacaoIndicativa(int classificacaoIndicativa) {
        this.classificacaoIndicativa = classificacaoIndicativa;
    }

    public float getPreco() {
        return preco;
    }

    public void setPreco(float preco) {
        this.preco = preco;
    }

    public int getAnoLancamento() {
        return anoLancamento;
    }

    public void setAnoLancamento(int anoLancamento) {
        this.anoLancamento = anoLancamento;
    }

    // Implementação do método toByteArray()
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(this.id);
        dos.writeUTF(this.titulo);
        dos.writeUTF(this.desenvolvedora);
        dos.writeInt(this.classificacaoIndicativa);
        dos.writeFloat(this.preco);
        dos.writeInt(this.anoLancamento);
        return baos.toByteArray();
    }

    // Implementação do método fromByteArray()
    public void fromByteArray(byte[] b) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        DataInputStream dis = new DataInputStream(bais);
        this.id = dis.readInt();
        this.titulo = dis.readUTF();
        this.desenvolvedora = dis.readUTF();
        this.classificacaoIndicativa = dis.readInt();
        this.preco = dis.readFloat();
        this.anoLancamento = dis.readInt();
    }

    @Override
    public String toString() {
        return "\nID....................: " + this.id +
               "\nTítulo................: " + this.titulo +
               "\nDesenvolvedora........: " + this.desenvolvedora +
               "\nClassificação Indic...: " + this.classificacaoIndicativa +
               "\nPreço.................: " + this.preco +
               "\nAno de Lançamento.....: " + this.anoLancamento;
    }
}