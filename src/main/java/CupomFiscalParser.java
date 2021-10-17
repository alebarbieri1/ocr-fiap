import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

import net.ricecode.similarity.JaroWinklerStrategy;
import net.ricecode.similarity.SimilarityStrategy;
import net.ricecode.similarity.StringSimilarityService;
import net.ricecode.similarity.StringSimilarityServiceImpl;
import java.text.Normalizer;

public class CupomFiscalParser {
    public static CupomFiscal parseConteudoOCR(String conteudo){
        if (conteudo == null || conteudo.isEmpty())
            return null;

        var cupomFiscal = new CupomFiscal();
        var reader = new ContentReader(conteudo);

        carregarRazaoSocial(cupomFiscal, reader);
        carregarEndereco(cupomFiscal, reader);
        carregarCnpj(cupomFiscal, reader);
        carregarIe(cupomFiscal, reader);
        carregarIm(cupomFiscal, reader);
        carregarDataHoraCcfCod(cupomFiscal, reader);
        carregarItens(cupomFiscal, reader);
        carregarSubtotal(cupomFiscal, reader);
        carregarDesconto(cupomFiscal, reader);
        carregarTotal(cupomFiscal, reader);

        return cupomFiscal;
    }

    private static void carregarRazaoSocial(CupomFiscal cupomFiscal, ContentReader reader){
        var linhasRazaoSocial = new ArrayList<String>();
        for (String linha; !(linha = reader.lerProximaLinha()).contains(",");)
            linhasRazaoSocial.add(linha);
        cupomFiscal.setRazaoSocial(String.join(" ", linhasRazaoSocial));
        reader.voltarLinha();
    }

    private static void carregarEndereco(CupomFiscal cupomFiscal, ContentReader reader){
        cupomFiscal.setEnderecoLinha1(reader.lerProximaLinha());
        var linha2 = reader.lerLinhaAtual();
        if (!isLinhaCnpj(linha2)) {
            cupomFiscal.setEnderecoLinha2(linha2);
            reader.proximaLinha();
        }
    }

    private static void carregarCnpj(CupomFiscal cupomFiscal, ContentReader reader){
        var linha = reader.lerLinhaAtual();
        if (!isLinhaCnpj(linha))
            return;

        linha = linha
                .replace("CNPJ", "")
                .replace(":", "")
                .replace(".", "")
                .replace("/", "")
                .replace("-", "")
                .replace(" ", "");

        if (linha.length() >= 14)
            cupomFiscal.setCnpj(linha.substring(0, 14));

        reader.proximaLinha();
    }

    private static void carregarIe(CupomFiscal cupomFiscal, ContentReader reader){
        var linha = reader.lerLinhaAtual();
        if (linha == null || !(linha.toUpperCase().contains("IE") || linha.toUpperCase().contains("1E")))
            return;

        cupomFiscal.setIe(obterValor(linha, ":"));
        reader.proximaLinha();
    }

    private static void carregarIm(CupomFiscal cupomFiscal, ContentReader reader){
        var linha = reader.lerLinhaAtual();
        if (linha == null || !linha.toUpperCase().contains("IM"))
            return;

        cupomFiscal.setIm(obterValor(linha, ":"));
        reader.proximaLinha();
    }

    private static void carregarDataHoraCcfCod(CupomFiscal cupomFiscal, ContentReader reader){
        var linha = reader.lerLinhaAtual();
        if (!isData(linha))
            return;

        cupomFiscal.setData(linha.substring(0, 10));

        for(String dado : linha.split(" ")){
            if (dado == null || dado.isEmpty())
                continue;
            var item = dado.trim();
            if (isHora(item))
                cupomFiscal.setHora(item.substring(0, 8));
            else {
                var valor = obterValor(item, ":");
                if (item.toUpperCase().contains("CCF"))
                    cupomFiscal.setCcf(valor);
                if (item.toUpperCase().contains("COO"))
                    cupomFiscal.setCoo(valor);

            }
        }

        reader.proximaLinha();
    }

    private static void carregarItens(CupomFiscal cupomFiscal, ContentReader reader){
        if (!encontraPrimeiroItem(reader))
            return;

        var itens = new ArrayList<ItemCupomFiscal>();
        var linha = reader.lerLinhaAtual();
        while (isItem(linha)){
            reader.proximaLinha();
            var linha1 = linha;
            var linha2 = reader.lerProximaLinha();
            var item = new ItemCupomFiscal();

            var dados = Arrays.stream(linha1.split(" ")).filter(x -> !x.isEmpty()).toArray(String[]::new);
            if (dados.length >= 1)
                item.setNumero(dados[0].trim());
            if (dados.length >= 2)
                item.setCodigo(dados[1].trim());
            if (dados.length >= 3)
                item.setDescricao(linha1.substring(linha1.indexOf(dados[2])).trim());

            dados = Arrays.stream(linha2.toUpperCase().split("X")).filter(x -> !x.isEmpty()).toArray(String[]::new);
            if (dados.length >= 1)
                item.setQuantidade(dados[0].trim());
            if (dados.length >= 2)
                item.setValorUnitario(dados[1].trim().split(" ")[0]);

            itens.add(item);

            linha = reader.lerLinhaAtual();
        }

        cupomFiscal.setItens(itens.toArray(ItemCupomFiscal[]::new));
    }

    private static boolean encontraPrimeiroItem(ContentReader reader){
        var linha = "";

        //Encontra cabeçalho das linhas
        while(!linha.toUpperCase().contains("ITEM") && !linha.toUpperCase().contains("1TEM") && !(isFimItens(linha))){
            linha = reader.lerProximaLinha();
            if (linha == null)
                break;
        }
        if (linha == null)
            return false;

        //Encontra primeiro item
        while(!isItem(linha)){
            linha = reader.lerProximaLinha();
            if (isFimItens(linha))
                linha = null;
            if (linha == null)
                break;
        }
        if (linha == null)
            return false;

        reader.voltarLinha();
        return true;
    }

    private static void carregarSubtotal(CupomFiscal cupomFiscal, ContentReader reader){
        var valor = lerTotalizador(reader, "SUBTOTAL");
        if (valor != null)
            cupomFiscal.setSubTotal(valor);
    }

    private static void carregarDesconto(CupomFiscal cupomFiscal, ContentReader reader){
        var valor = lerTotalizador(reader, "DESCONTO");
        if (valor != null)
            cupomFiscal.setDesconto(valor);
    }

    private static void carregarTotal(CupomFiscal cupomFiscal, ContentReader reader){
        var valor = lerTotalizador(reader, "TOTAL");
        if (valor != null)
            cupomFiscal.setTotal(valor);
    }

    private static String lerTotalizador(ContentReader reader, String id){
        var linha = reader.lerLinhaAtual();
        if (linha == null || !linha.toUpperCase().contains(id))
            return null;

        reader.proximaLinha();
        return obterValor(linha, " ").trim();
    }

    private static String obterValor(String s, String separador){
        var descricaoValor = s.split(separador);
        return descricaoValor[descricaoValor.length -1];
    }

    private static boolean isLinhaCnpj(String linha){
        if(linha == null)
            return false;
        
        if(linha.toUpperCase().contains("CNPJ"))
            return true;
        
        Pattern pattern = Pattern.compile("\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2}");
        return pattern.matcher(linha).find();
    }

    private static boolean isData(String s){
        return s != null && s.length() >= 10 && s.charAt(2) == '/' && s.charAt(5) == '/';
    }

    private static boolean isHora(String s){
        return s != null && s.length() >= 8 && s.charAt(2) == ':' && s.charAt(5) == ':';
    }

    private static boolean isFimItens(String s){
        return (s != null && s.toUpperCase().contains("TOTAL")) || (s != null && s.toUpperCase().contains("DESCONTO"));
    }

    private static boolean isItem(String s){
        return s != null && s.substring(0, 3).chars().allMatch(Character::isDigit);
    }

    private static boolean contemPalavraSimilar(String string, String palavraAPesquisar, double limiarMinimo){
        if(palavraAPesquisar == null || string == null)
            return false;
        
        SimilarityStrategy strategy = new JaroWinklerStrategy();
        StringSimilarityService service = new StringSimilarityServiceImpl(strategy);
        String stringTratada = tratarString(string);
        String palavraTratada = tratarString(palavraAPesquisar);

        for(String palavra : stringTratada.split(" ")){
            if(service.score(palavra, palavraTratada) > limiarMinimo)
            return true;
        }
        return false;
    }

    public static String tratarString(String s){
        return Normalizer.normalize(s, Normalizer.Form.NFD)
            .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
            .toLowerCase()
            .replaceAll("\\s+", " ")
            .trim();
    }
}
