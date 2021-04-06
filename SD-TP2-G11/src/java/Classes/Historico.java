/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Classes;

/**
 *
 * @author sergio
 */
public class Historico {
    
    private int idmat;
    private String tipo;
    private String name;
    private double preco;
    private String descricao;
    private int num;
    private String username;
    private String acao;

    public Historico(int id, String tip, String nam, double prec, String desc, int top3, String usernam, String action) {

        this.idmat = id;
        this.tipo = tip;
        this.name = nam;
        this.preco = prec;
        this.descricao = desc;
        this.num = top3;
        this.username = usernam;
        this.acao = action;
    }

    public String apresentarprec(String prec) {

        return prec + "€";

    }

    public String apresentadesc(String desc) {

        String frase = "";
        for (int i = 0; i < desc.length(); i++) {

            if (i % 28 == 0) {

                if (i == 0) {
                    frase += desc.charAt(i);

                } else if (desc.charAt(i) == ' ') {

                    frase += "\n";

                } else {

                    frase += desc.charAt(i) + "-\n";

                }

            } else {

                frase += desc.charAt(i);
            }
        }

        return frase;

    }

    public int getIdmat() {
        return idmat;
    }

    public void setIdmat(int idmat) {
        this.idmat = idmat;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPreco() {
        return preco;
    }

    public void setPreco(double preco) {
        this.preco = preco;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAcao() {
        return acao;
    }

    public void setAcao(String acao) {
        this.acao = acao;
    }

    @Override
    public String toString() {
        return "Historico{" + "idmat=" + idmat + ", tipo=" + tipo + ", name=" + name + ", preco=" + preco + ", descricao=" + descricao + ", num=" + num + ", username=" + username + ", acao=" + acao + '}';
    }

  
    
    
    
}
