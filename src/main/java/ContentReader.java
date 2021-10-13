import java.util.Arrays;

public class ContentReader {
    private int indice;
    private String conteudo;
    private String[] linhas;
    public ContentReader(String conteudo){
        this.setConteudo(conteudo);
    }

    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.reiniciar();
        this.conteudo = conteudo;
        this.linhas = obtemLinhas(conteudo);
    }

    public String lerProximaLinha(){
        return this.lerLinha(this.indice++);
    }

    public String lerLinhaAtual(){
        return this.lerLinha(this.indice);
    }

    private String lerLinha(int indice){
        return (this.linhas.length > indice) ? this.linhas[indice] : null;
    }

    public void reiniciar(){
        this.indice = 0;
    }

    public void proximaLinha(){
        this.indice++;
    }

    public void voltarLinha(){
        this.indice--;
    }

    public int getQuantidadeLinhas(){
        return (this.linhas != null) ? this.linhas.length : 0;
    }

    private String[] obtemLinhas(String conteudo){
        var linhas = conteudo.split("\n");
        return Arrays.stream(linhas).filter(x -> !x.isEmpty()).toArray(String[]::new);
    }
}
