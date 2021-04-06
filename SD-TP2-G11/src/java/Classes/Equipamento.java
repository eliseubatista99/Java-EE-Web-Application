package Classes;

import java.io.Serializable;

public class Equipamento implements Serializable {

    private int idmat;
    private String tipo;
    private String name;
    private double preco;
    private String descricao;
    private String username;

    public Equipamento(int id, String tip, String nam, double prec, String desc, String user) {

        this.idmat = id;
        this.tipo = tip;
        this.name = nam;
        this.preco = prec;
        this.descricao = desc;
        this.username = user;

    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public int getIdmat() {
        return idmat;
    }

    public void setIdmat(int idmat) {
        this.idmat = idmat;
    }

    @Override
    public String toString() {
        return "Equipamento{" + "idmat=" + idmat + ", tipo=" + tipo + ", name=" + name + ", preco=" + preco + ", descricao=" + descricao + '}';
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

}
