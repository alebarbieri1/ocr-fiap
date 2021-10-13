public class CupomFiscal {
    private String razaoSocial;
    private String enderecoLinha1;
    private String enderecoLinha2;
    private String cnpj;
    private String ie;
    private String im;
    private String data;
    private String hora;
    private String ccf;
    private String coo;
    private ItemCupomFiscal[] itens;
    private String subTotal;
    private String desconto;
    private String total;

    public String toString(){
        var s =
            "RAZÃO SOCIAL = " + razaoSocial + "\n" +
            "ENDEREÇO LINHA 1 = " + enderecoLinha1 + "\n" +
            "ENDEREÇO LINHA 2 = " + enderecoLinha2 + "\n" +
            "CNPJ = " + cnpj + "\n" +
            "IE = " + ie + "\n" +
            "IM = " + im + "\n" +
            "DATA = " + data + "\n" +
            "HORA = " + hora + "\n" +
            "CCF = " + ccf + "\n" +
            "COO = " + coo + "\n";

        for(int i = 0; i < itens.length; i++){
            s +=
                    "\tITEM #" + (i + 1) + "\n" +
                    "\tNÚMERO:" + itens[i].getNumero() + "\n" +
                    "\tCÓDIGO:" + itens[i].getCodigo() + "\n" +
                    "\tDESCRIÇÃO:" + itens[i].getDescricao() + "\n" +
                    "\tVALOR UNITÁRIO:" + itens[i].getValorUnitario() + "\n" +
                    "\tQUANTIDADE:" + itens[i].getQuantidade() + "\n";
        }

        s +=
            "SUB TOTAL = " + subTotal + "\n" +
            "DESCONTO = " + desconto + "\n" +
            "TOTAL = " + total + "\n";

        return s;
    }

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public void setRazaoSocial(String razaoSocial) {
        this.razaoSocial = razaoSocial;
    }

    public String getEnderecoLinha1() {
        return enderecoLinha1;
    }

    public void setEnderecoLinha1(String enderecoLinha1) {
        this.enderecoLinha1 = enderecoLinha1;
    }

    public String getEnderecoLinha2() {
        return enderecoLinha2;
    }

    public void setEnderecoLinha2(String enderecoLinha2) {
        this.enderecoLinha2 = enderecoLinha2;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getIe() {
        return ie;
    }

    public void setIe(String ie) {
        this.ie = ie;
    }

    public String getIm() {
        return im;
    }

    public void setIm(String im) {
        this.im = im;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public String getCcf() {
        return ccf;
    }

    public void setCcf(String ccf) {
        this.ccf = ccf;
    }

    public String getCoo() {
        return coo;
    }

    public void setCoo(String coo) {
        this.coo = coo;
    }

    public ItemCupomFiscal[] getItens() {
        return itens;
    }

    public void setItens(ItemCupomFiscal[] itens) {
        this.itens = itens;
    }

    public String getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(String subTotal) {
        this.subTotal = subTotal;
    }

    public String getDesconto() {
        return desconto;
    }

    public void setDesconto(String desconto) {
        this.desconto = desconto;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }
}
