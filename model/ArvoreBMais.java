package model;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;

/**
 * Índice secundário em Árvore B+ sobre uma chave estrangeira.
 * Chave: int (ex.: Pedido.idCliente). Referência: long (endereço do
 * registro correspondente no arquivo de dados, para acesso direto via
 * Arquivo.lerNoEndereco).
 *
 * Só as folhas guardam (chave, referência); nós internos guardam apenas
 * chaves separadoras e ponteiros de filho. As folhas são encadeadas entre
 * si para permitir varrer sequencialmente as ocorrências de uma chave
 * duplicada sem subir/descer na árvore.
 *
 * Simplificação assumida: a remoção apaga a entrada da folha mas não faz
 * fusão/redistribuição de nós subdimensionados. Isso não compromete a
 * corretude da busca (os limites de separação entre filhos continuam
 * válidos), só a compactação da árvore ao longo do tempo.
 */
public class ArvoreBMais {
    private static final int ORDEM = 4;
    private static final int MAX_CHAVES = ORDEM - 1;
    private static final int TAM_CABECALHO = 8;

    private static final byte FOLHA = 'F';
    private static final byte INTERNO = 'I';

    private RandomAccessFile arquivo;

    private static class Pagina {
        byte tipo;
        int n;
        long endereco;
        int[] chaves = new int[MAX_CHAVES + 1];
        long[] referencias = new long[MAX_CHAVES + 1]; // usado só em folhas
        long[] ponteiros = new long[ORDEM + 1];         // usado só em internos
        long proximaFolha = -1;                          // usado só em folhas
    }

    private static class ResultadoSplit {
        int chavePromovida;
        long novaPagina;
    }

    public ArvoreBMais(String nomeArquivo) throws Exception {
        File arquivoFisico = new File(nomeArquivo);
        File diretorio = arquivoFisico.getParentFile();
        if (diretorio != null && !diretorio.exists()) diretorio.mkdirs();

        this.arquivo = new RandomAccessFile(nomeArquivo, "rw");
        if (arquivo.length() < TAM_CABECALHO) {
            arquivo.writeLong(-1); // raiz inexistente
        }
    }

    private long getRaiz() throws Exception {
        arquivo.seek(0);
        return arquivo.readLong();
    }

    private void setRaiz(long endereco) throws Exception {
        arquivo.seek(0);
        arquivo.writeLong(endereco);
    }

    private Pagina lerPagina(long endereco) throws Exception {
        arquivo.seek(endereco);
        Pagina p = new Pagina();
        p.endereco = endereco;
        p.tipo = arquivo.readByte();
        p.n = arquivo.readInt();

        if (p.tipo == FOLHA) {
            for (int i = 0; i < MAX_CHAVES; i++) {
                p.chaves[i] = arquivo.readInt();
                p.referencias[i] = arquivo.readLong();
            }
            p.proximaFolha = arquivo.readLong();
        } else {
            p.ponteiros[0] = arquivo.readLong();
            for (int i = 0; i < MAX_CHAVES; i++) {
                p.chaves[i] = arquivo.readInt();
                p.ponteiros[i + 1] = arquivo.readLong();
            }
        }
        return p;
    }

    private void escreverPagina(Pagina p) throws Exception {
        arquivo.seek(p.endereco);
        arquivo.writeByte(p.tipo);
        arquivo.writeInt(p.n);

        if (p.tipo == FOLHA) {
            for (int i = 0; i < MAX_CHAVES; i++) {
                arquivo.writeInt(p.chaves[i]);
                arquivo.writeLong(p.referencias[i]);
            }
            arquivo.writeLong(p.proximaFolha);
        } else {
            arquivo.writeLong(p.ponteiros[0]);
            for (int i = 0; i < MAX_CHAVES; i++) {
                arquivo.writeInt(p.chaves[i]);
                arquivo.writeLong(p.ponteiros[i + 1]);
            }
        }
    }

    private long novaPagina(byte tipo) throws Exception {
        long endereco = Math.max(arquivo.length(), TAM_CABECALHO);
        Pagina p = new Pagina();
        p.endereco = endereco;
        p.tipo = tipo;
        p.n = 0;
        for (int i = 0; i < MAX_CHAVES; i++) p.chaves[i] = -1;
        if (tipo == FOLHA) {
            p.proximaFolha = -1;
        } else {
            for (int i = 0; i <= MAX_CHAVES; i++) p.ponteiros[i] = -1;
        }
        escreverPagina(p);
        return endereco;
    }

    // ---------------------------------------------------------------
    // Inserção
    // ---------------------------------------------------------------

    public void inserir(int chave, long referencia) throws Exception {
        long enderecoRaiz = getRaiz();
        if (enderecoRaiz == -1) {
            enderecoRaiz = novaPagina(FOLHA);
            setRaiz(enderecoRaiz);
        }

        ResultadoSplit resultado = inserir(enderecoRaiz, chave, referencia);
        if (resultado != null) {
            long novaRaizEndereco = novaPagina(INTERNO);
            Pagina novaRaiz = lerPagina(novaRaizEndereco);
            novaRaiz.ponteiros[0] = enderecoRaiz;
            novaRaiz.chaves[0] = resultado.chavePromovida;
            novaRaiz.ponteiros[1] = resultado.novaPagina;
            novaRaiz.n = 1;
            escreverPagina(novaRaiz);
            setRaiz(novaRaizEndereco);
        }
    }

    private ResultadoSplit inserir(long endereco, int chave, long referencia) throws Exception {
        Pagina p = lerPagina(endereco);

        if (p.tipo == FOLHA) {
            inserirOrdenadoFolha(p, chave, referencia);
            if (p.n <= MAX_CHAVES) {
                escreverPagina(p);
                return null;
            }
            return dividirFolha(p);
        }

        // empate vai para a esquerda: garante que, ao buscar, a primeira
        // ocorrência de uma chave duplicada nunca fique numa folha à
        // esquerda da folha em que a busca começa a olhar
        int i = 0;
        while (i < p.n && chave > p.chaves[i]) i++;
        ResultadoSplit resultadoFilho = inserir(p.ponteiros[i], chave, referencia);
        if (resultadoFilho == null) return null;

        // usa a posição i já conhecida (o filho que dividiu), em vez de
        // redescobri-la comparando a chave promovida: se essa chave for
        // igual a uma chave já existente no nó (FK duplicada), uma nova
        // busca por comparação não distingue a cópia nova da antiga e
        // insere o ponteiro na posição errada.
        inserirNaPosicaoInterno(p, i, resultadoFilho.chavePromovida, resultadoFilho.novaPagina);
        if (p.n <= MAX_CHAVES) {
            escreverPagina(p);
            return null;
        }
        return dividirInterno(p);
    }

    private void inserirOrdenadoFolha(Pagina p, int chave, long referencia) {
        int i = p.n - 1;
        while (i >= 0 && (p.chaves[i] > chave || (p.chaves[i] == chave && p.referencias[i] > referencia))) {
            p.chaves[i + 1] = p.chaves[i];
            p.referencias[i + 1] = p.referencias[i];
            i--;
        }
        p.chaves[i + 1] = chave;
        p.referencias[i + 1] = referencia;
        p.n++;
    }

    private void inserirNaPosicaoInterno(Pagina p, int pos, int chave, long ponteiroDireito) {
        for (int j = p.n; j > pos; j--) {
            p.chaves[j] = p.chaves[j - 1];
            p.ponteiros[j + 1] = p.ponteiros[j];
        }
        p.chaves[pos] = chave;
        p.ponteiros[pos + 1] = ponteiroDireito;
        p.n++;
    }

    // Ao dividir uma folha, a chave mediana é copiada para o pai e
    // mantida (repetida) na nova folha da direita.
    private ResultadoSplit dividirFolha(Pagina p) throws Exception {
        int total = p.n;
        int meio = total / 2;

        long enderecoNovaFolha = novaPagina(FOLHA);
        Pagina nova = lerPagina(enderecoNovaFolha);

        nova.n = total - meio;
        for (int i = 0; i < nova.n; i++) {
            nova.chaves[i] = p.chaves[meio + i];
            nova.referencias[i] = p.referencias[meio + i];
        }

        nova.proximaFolha = p.proximaFolha;
        p.proximaFolha = enderecoNovaFolha;
        p.n = meio;

        escreverPagina(p);
        escreverPagina(nova);

        ResultadoSplit resultado = new ResultadoSplit();
        resultado.chavePromovida = nova.chaves[0];
        resultado.novaPagina = enderecoNovaFolha;
        return resultado;
    }

    // Ao dividir um nó interno, a chave mediana sobe inteiramente para o
    // pai (não fica duplicada em nenhum dos dois filhos resultantes).
    private ResultadoSplit dividirInterno(Pagina p) throws Exception {
        int total = p.n;
        int meio = total / 2;
        int chavePromovida = p.chaves[meio];

        long enderecoNovoInterno = novaPagina(INTERNO);
        Pagina novo = lerPagina(enderecoNovoInterno);

        novo.n = total - meio - 1;
        for (int i = 0; i < novo.n; i++) {
            novo.chaves[i] = p.chaves[meio + 1 + i];
        }
        for (int i = 0; i <= novo.n; i++) {
            novo.ponteiros[i] = p.ponteiros[meio + 1 + i];
        }

        p.n = meio;

        escreverPagina(p);
        escreverPagina(novo);

        ResultadoSplit resultado = new ResultadoSplit();
        resultado.chavePromovida = chavePromovida;
        resultado.novaPagina = enderecoNovoInterno;
        return resultado;
    }

    // ---------------------------------------------------------------
    // Busca — todas as referências associadas a uma chave (FK)
    // ---------------------------------------------------------------

    public ArrayList<Long> buscar(int chave) throws Exception {
        ArrayList<Long> resultado = new ArrayList<>();
        long enderecoRaiz = getRaiz();
        if (enderecoRaiz == -1) return resultado;

        Pagina folha = lerPagina(encontrarFolha(enderecoRaiz, chave));

        boolean continuar;
        do {
            continuar = false;
            for (int i = 0; i < folha.n; i++) {
                if (folha.chaves[i] == chave) {
                    resultado.add(folha.referencias[i]);
                }
            }
            // continua enquanto a última chave da folha não tiver ultrapassado
            // a chave buscada — não basta checar igualdade: como a busca pode
            // ter entrado numa folha à esquerda que não contém a chave (mas
            // cuja folha seguinte contém), o critério certo é "ainda não vi
            // nada maior que a chave procurada"
            if (folha.proximaFolha != -1 && (folha.n == 0 || folha.chaves[folha.n - 1] <= chave)) {
                folha = lerPagina(folha.proximaFolha);
                continuar = true;
            }
        } while (continuar);

        return resultado;
    }

    private long encontrarFolha(long endereco, int chave) throws Exception {
        Pagina p = lerPagina(endereco);
        while (p.tipo != FOLHA) {
            int i = 0;
            while (i < p.n && chave > p.chaves[i]) i++;
            p = lerPagina(p.ponteiros[i]);
        }
        return p.endereco;
    }

    // ---------------------------------------------------------------
    // Remoção de uma entrada (chave, referência) específica
    // ---------------------------------------------------------------

    public boolean remover(int chave, long referencia) throws Exception {
        long enderecoRaiz = getRaiz();
        if (enderecoRaiz == -1) return false;

        Pagina folha = lerPagina(encontrarFolha(enderecoRaiz, chave));

        // a entrada pode não estar na primeira folha encontrada: com chaves
        // duplicadas, o mesmo valor pode estar espalhado por várias folhas
        // encadeadas (igual em buscar()), então é preciso seguir a cadeia.
        while (true) {
            for (int i = 0; i < folha.n; i++) {
                if (folha.chaves[i] == chave && folha.referencias[i] == referencia) {
                    for (int j = i; j < folha.n - 1; j++) {
                        folha.chaves[j] = folha.chaves[j + 1];
                        folha.referencias[j] = folha.referencias[j + 1];
                    }
                    folha.n--;
                    escreverPagina(folha);
                    return true;
                }
            }

            if (folha.proximaFolha != -1 && (folha.n == 0 || folha.chaves[folha.n - 1] <= chave)) {
                folha = lerPagina(folha.proximaFolha);
            } else {
                return false;
            }
        }
    }

    public void close() throws Exception {
        arquivo.close();
    }
}
