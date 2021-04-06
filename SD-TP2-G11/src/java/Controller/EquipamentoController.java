package Controller;

import Classes.Equipamento;
import Classes.Historico;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateful;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;
import java.sql.PreparedStatement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;

@Stateful
@ManagedBean(name = "equipamento")
@SessionScoped
public class EquipamentoController implements Serializable {

    @EJB
    private UsersController usersController;
    private List<Equipamento> equipamentosReservados = new ArrayList<>(); //Lista de Equipamentos reservados pelo utilizador
    private List<Equipamento> equipamentosAlugados = new ArrayList<>(); // Lista de equipamentos alugados pelo utilizador
    private List<Equipamento> equipamentos = new ArrayList<>(); //Lista de equipamentos geral
    private List<Integer> idEquipamentosUser = new ArrayList<>(); // Listas dos IDs dos equipamentos do utilizador
    private List<Integer> idAlugados = new ArrayList<>(); //Lista dos ids dos equipamentos alugados pelo utilizador
    private List<Integer> idReservados = new ArrayList<>(); //Lista dos ids dos equipamentos reservados pelo utilizador
    private List<Integer> idhistorico = new ArrayList<>(); //Lista dos ids dos equipamentos do historico de movimentos
    private List<String> tipoHistorico = new ArrayList<>(); //Lista temporária para armazenar o tipo de ação realizada pelo utilizador
    private List<Historico> historico = new ArrayList<>(); //Lista dos equipamentos do historico de movimentos
    private List<Integer> idtop3 = new ArrayList<>(); //Lista dos ids dos equipamentos do top3 de equipamentos com mais interações
    private List<Historico> listTop3 = new ArrayList<>(); //Lista dos equipamentos do top3 de equipamentos com mais interações
    private List<Integer> idNews = new ArrayList<>(); //Lista dos ids dos equipamentos mais recentes
    private List<Equipamento> listNews = new ArrayList<>(); //Lista dos equipamentos mais recentes
    private double contacorrente = 0.0; //Valor da conta corrente do utilizador

    /** ---------------------------------------------------------------------------------------------------------------------------------------------
     * Função para atualizar a tabela de equipamentos e reencaminhar o
     * utilizador para a respetiva página, consoante este esteja ou não logado
     *
     * @param logged
     * @throws java.sql.SQLException
     * @throws java.io.IOException
     */
    public void atualizarEquipamentos(boolean logged) throws SQLException, IOException {
        HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
        //Atualizar tabela
        atualizarTabelaEquipamentos();
        //Reencaminhar utilizador
        if (logged) {
            response.sendRedirect("inventorylogged.xhtml");
        } else {
            response.sendRedirect("inventoryunlogged.xhtml");
        }
    }

    /** ---------------------------------------------------------------------------------------------------------------------------------------------
     * Atualizar tabela de equipamentos
     * 
     * @throws SQLException
     * @throws IOException 
     */
    public void atualizarTabelaEquipamentos() throws SQLException, IOException {
        //Limpar lista de equipamentos
        this.equipamentos.clear();
        Connection con = DriverManager.getConnection("jdbc:derby://localhost:1527/Projeto", "app", "app");
        Statement ps = con.createStatement();
        try {
            //Query (tudo da tabela equipamentos)
            ResultSet x = ps.executeQuery("SELECT * FROM EQUIPAMENTOS ");
            //Iterar pelo resultado da query
            while (x.next()) {
                //Obter dados
                int idmat = x.getInt("IDMAT");
                String tipo = x.getString("TIPO");
                String name = x.getString("NAME");
                double preco = x.getDouble("PRECO");
                String descricao = x.getString("DESCRICAO");
                String username = x.getString("USERNAME");
                //Instanciar um equipamento com os dados lidos
                Equipamento equip = new Equipamento(idmat, tipo, name, preco, descricao, username);
                //Adicionar à lista
                this.equipamentos.add(equip);
            }
        } catch (SQLException e) {
        }
        con.close();
        ps.close();

    }

    /** ---------------------------------------------------------------------------------------------------------------------------------------------
     * Atualizar o valor da conta corrente 
     * 
     * @param username
     * @throws SQLException
     * @throws IOException 
     */
    public void getcontacorrente(String username) throws SQLException, IOException {
        //Configurar as listas de equipamentos e de ids de equipamentos para o utilizador
        configurarEquipamentosUser(username, false);
        double count = 0.0;
        //Percorrer a lista de equipamentos alugados
        for (int i = 0; i < equipamentosAlugados.size(); i++) {
            //Somatório dos preços
            count += equipamentosAlugados.get(i).getPreco();
        }
        //Atualizar valor da conta corrente
        this.contacorrente = count;
    }

    /** ---------------------------------------------------------------------------------------------------------------------------------------------
     * Configurar as listas de equipamentos e de ids de equipamentos para o utilizador
     * 
     * @param username
     * @param willRedirect 
     */
    public void configurarEquipamentosUser(String username, boolean willRedirect) {
        //Limpar listas-------
        this.idEquipamentosUser.clear();
        this.idReservados.clear();
        this.idAlugados.clear();
        this.equipamentosAlugados.clear();
        this.equipamentosReservados.clear();
        //---------------------
        HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
        // connection to the database
        Connection con = null;
        Statement ps = null;
        try {
            con = DriverManager.getConnection("jdbc:derby://localhost:1527/Projeto", "app", "app");
            ps = con.createStatement();
            //Configurar listas de ids de equipamentos
            configurarEquipamentosUserID(ps, username);
            //Atualizar tabela de equipamentos
            atualizarTabelaEquipamentos();
            //Configurar listas de equipamentos
            configurarEquipamentosUserEquip(ps, username);
            con.close();
            ps.close();
            //reencaminhar
            if (willRedirect) {
                response.sendRedirect("page.xhtml");
            }
        } catch (SQLException ex) {
            Logger.getLogger(EquipamentoController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(EquipamentoController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /** ---------------------------------------------------------------------------------------------------------------------------------------------
     * Configurar listas de ids de equipamentos do utilizador
     * 
     * @param ps
     * @param username 
     */
    public void configurarEquipamentosUserID(Statement ps, String username) {
        try {
            //Query (tudo da tabela equipusers, com o USERNAME = username)
            ResultSet x = ps.executeQuery("SELECT * FROM EQUIPUSERS WHERE USERNAME='" + username + "'");
            //Iterar resultados
            while (x.next()) {
                //Obter id do equipamento
                int idmat = x.getInt("IDMAT");
                //Adicionar à lista
                this.idEquipamentosUser.add(idmat);
            }
        } catch (SQLException e) {
        }

        //Percorrer lista de ids de equipamentos do utilizador
        for (int i = 0; i < idEquipamentosUser.size(); i++) {
            int min_id = 9999;
            String tmp_user = "";
            try {
                //Query (tudo da tabela equipusers, com o IDMAT = elemento da lista idEquipamentosUser)
                ResultSet x = ps.executeQuery("SELECT * FROM EQUIPUSERS WHERE IDMAT=" + idEquipamentosUser.get(i));
                //Iterar
                while (x.next()) {
                    //Obter id da linha da tabela
                    int id = x.getInt("ID");
                    //Se for menor que o id armazenado anteriormente
                    if (id < min_id) {
                        //Guardar menor
                        min_id = id;
                        //Guardar username
                        tmp_user = x.getString("USERNAME");
                    }
                }
                //Se o username for o do user atual, ele alugou o equipamento
                if (username.equals(tmp_user)) {
                    //Adicionar à lista de ids de equipamentos alugados
                    this.idAlugados.add(idEquipamentosUser.get(i));
                } //Caso contrário, ele reservou o equipamento
                else {
                    //Adicionar à lista de ids de equipamentos reservados
                    this.idReservados.add(idEquipamentosUser.get(i));
                }
            } catch (SQLException e) {
            }
        }
    }

    /** ---------------------------------------------------------------------------------------------------------------------------------------------
     * Obtem os 3 equipamentos com que os utilizadores mais interagiram
     * 
     * @throws SQLException 
     */
    public void top3() throws SQLException {

        //Limpar as listas
        this.listTop3.clear();
        this.idtop3.clear();
        //Utilizada para apresentar a frequencia
        List<Integer> count = new ArrayList<>();

        Connection con = DriverManager.getConnection("jdbc:derby://localhost:1527/Projeto", "app", "app");
        Statement ps = con.createStatement();

        int b = 0;

        try {
            //Query, obter os IDMATS mais frequentes da tabela histórico, ordenados decrescentemente
            ResultSet x = ps.executeQuery("SELECT IDMAT, COUNT(IDMAT) AS top3 FROM HISTORICO GROUP BY IDMAT ORDER BY top3 DESC");
            //Iterar pelo resultado da query
            while (x.next()) {

                int idmat = x.getInt("IDMAT");
                this.idtop3.add(idmat);
                b++;
                count.add(x.getInt("top3"));
                //Contar apenas o top3
                if (b == 3) {
                    break;
                }
            }
        } catch (SQLException e) {
        }
        //Percorrer lista de ids do top3
        for (int i = 0; i < idtop3.size(); i++) {
            try {
                //Query, obter os equipamentos do top3
                ResultSet x = ps.executeQuery("SELECT * FROM EQUIPAMENTOS WHERE IDMAT=" + idtop3.get(i));
                //Iterar
                while (x.next()) {

                    int id = x.getInt("IDMAT");
                    String tipo = x.getString("TIPO");
                    String name = x.getString("NAME");
                    double preco = x.getDouble("PRECO");
                    String descricao = x.getString("DESCRICAO");
                    int contador = count.get(i);
                    //Criar um objeto historico (username e tipo irrelevantes)
                    Historico equip = new Historico(id, tipo, name, preco, descricao, contador, "", "");
                    //Adicionar esse objeto a top3list
                    this.listTop3.add(equip);
                }
            } catch (SQLException e) {
            }
        }
        con.close();
        ps.close();

    }

    /** --------------------------------------------------------------------------------------------------------------------------------------------- 
     * Obtem os 3 equipamentos mais recentes
     * 
     * @throws SQLException 
     */
    public void newsGetter() throws SQLException {
        //Limpar as listas
        this.listNews.clear();
        this.idNews.clear();

        Connection con = DriverManager.getConnection("jdbc:derby://localhost:1527/Projeto", "app", "app");
        Statement ps = con.createStatement();

        int aux = 0;

        try {
            //Query para obter os IDMATS mais recentes
            ResultSet x = ps.executeQuery("SELECT IDMAT FROM EQUIPAMENTOS ORDER BY IDMAT DESC");
            //Iterar pelo resultado da query
            while (x.next()) {
                //System.out.println("entrou");
                int idmat = x.getInt("IDMAT");
                this.idNews.add(idmat);
                aux++;
                if (aux == 3) {
                    break;
                }

            }
            //System.out.println(idNews);
        } catch (SQLException e) {
        }
        System.out.println("arrayzinho" + idNews);
        //Percorrer a lista das news
        for (int i = 0; i < idNews.size(); i++) {
            try {
                //Query, obter os equipamentos novidade
                ResultSet x = ps.executeQuery("SELECT * FROM EQUIPAMENTOS WHERE IDMAT=" + idNews.get(i));
                //run listinha
                while (x.next()) {

                    int id = x.getInt("IDMAT");
                    String tipo = x.getString("TIPO");
                    String name = x.getString("NAME");
                    double preco = x.getDouble("PRECO");
                    String descricao = x.getString("DESCRICAO");
                    //Criar um objeto equipamento
                    Equipamento equip = new Equipamento(id, tipo, name, preco, descricao, "");
                    //Adicionar esse objeto a news list
                    this.listNews.add(equip);
                }
            } catch (SQLException e) {
            }
        }
        con.close();
        ps.close();

    }

    /** --------------------------------------------------------------------------------------------------------------------------------------------- 
     * Prepara a lista com os ultimos movimentos do utilizador
     * 
     * @param username
     * @throws SQLException 
     */
    public void ultimosMove(String username) throws SQLException {
        //Refresh, limpar as listas
        this.idhistorico.clear();
        this.historico.clear();
        this.tipoHistorico.clear();
        Connection con = DriverManager.getConnection("jdbc:derby://localhost:1527/Projeto", "app", "app");
        Statement ps = con.createStatement();
        //Variáveis
        Historico equip = null;
        int id = 0;
        String tipo = "";
        String name = "";
        double preco = 0;
        String descricao = "";
        //--------------------
        try {
            //Procurar o histórico do utilizador
            ResultSet x = ps.executeQuery("SELECT * FROM HISTORICO WHERE USERNAME='" + username + "'");

            while (x.next()) {
                //Obter id do equipamento
                int idmat = x.getInt("IDMAT");
                //Obter tipo de equipamento
                String acao = x.getString("TIPO");
                //Adicionar à lista
                this.idhistorico.add(idmat);
                this.tipoHistorico.add(acao);
            }

        } catch (SQLException e) {
        }

        //Percorrer lista de ids do historico do utilizador
        for (int i = 0; i < idhistorico.size(); i++) {
            try {
                //Query, obter os equipamentos do historico
                ResultSet x = ps.executeQuery("SELECT * FROM EQUIPAMENTOS WHERE IDMAT=" + idhistorico.get(i));
                //Iterar
                while (x.next()) {

                    id = x.getInt("IDMAT");
                    tipo = x.getString("TIPO");
                    name = x.getString("NAME");
                    preco = x.getDouble("PRECO");
                    descricao = x.getString("DESCRICAO");
                }
                //Criar um objeto histórico (a contagem é irrelevante)
                equip = new Historico(id, tipo, name, preco, descricao, 0, username, tipoHistorico.get(i));
                //Adicionar à lista
                this.historico.add(equip);
            } catch (SQLException e) {
            }
        }
        con.close();
        ps.close();

    }

    /** --------------------------------------------------------------------------------------------------------------------------------------------- 
     * Com base nos ids lidos, obter da tabela equipamentos os dados do equipamento correspondente
     * 
     * @param ps
     * @param username 
     */
    public void configurarEquipamentosUserEquip(Statement ps, String username) {
        for (int b = 0; b < idAlugados.size(); b++) {
            for (int c = 0; c < equipamentos.size(); c++) {
                if (idAlugados.get(b) == equipamentos.get(c).getIdmat()) {
                    this.equipamentosAlugados.add(equipamentos.get(c));
                }
            }
        }
        for (int b = 0; b < idReservados.size(); b++) {
            for (int c = 0; c < equipamentos.size(); c++) {
                if (idReservados.get(b) == equipamentos.get(c).getIdmat()) {
                    this.equipamentosReservados.add(equipamentos.get(c));
                }
            }
        }
    }

    /** ---------------------------------------------------------------------------------------------------------------------------------------------
     * Obter o ID do ultimo equipamento da tabela
     * 
     * @return 
     */
    public int getUltimoEquipamentoID() {
        // connection to the database
        Connection con = null;
        Statement ps1 = null;
        ResultSet x = null;
        int idMAT = 0;
        try {
            con = DriverManager.getConnection("jdbc:derby://localhost:1527/Projeto", "app", "app");
            ps1 = con.createStatement();
            x = ps1.executeQuery("SELECT * FROM EQUIPAMENTOS");

            while (x.next()) {
                // check the int value

                if (idMAT < x.getInt("IDMAT")) {

                    idMAT = x.getInt("IDMAT");

                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(EquipamentoController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return idMAT;
    }

    /** ---------------------------------------------------------------------------------------------------------------------------------------------
     * Registar um equipamento
     * 
     * @param tipoProduto
     * @param nomeProduto
     * @param preco
     * @param descricao
     * @param username
     * @throws SQLException
     * @throws IOException 
     */
    public void registarEquipamento(String tipoProduto, String nomeProduto, double preco, String descricao, String username) throws SQLException, IOException {
        Connection con = DriverManager.getConnection("jdbc:derby://localhost:1527/Projeto", "app", "app");
        Statement ps = con.createStatement();
        HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
        int regist = 0, erro = 0;
        int idMAT = getUltimoEquipamentoID();
        idMAT += 1;
        int nextID = 0;
        if (nomeProduto.equals("")) {
            erro = 1;
            FacesMessage fm = new FacesMessage("Campo nome do produto em branco!");
            FacesContext.getCurrentInstance().addMessage("msg", fm);
        }

        if (preco == 0.0) {
            erro = 1;
            FacesMessage fm = new FacesMessage("Campo preço em branco!");
            FacesContext.getCurrentInstance().addMessage("msg", fm);
        }

        if (descricao.equals("")) {
            erro = 1;
            FacesMessage fm = new FacesMessage("Campo descrição em branco!");
            FacesContext.getCurrentInstance().addMessage("msg", fm);
        }

        try {
            ResultSet x = ps.executeQuery("SELECT * FROM HISTORICO");
            while (x.next()) {
                // check the int value

                if (nextID < x.getInt("ID") || nextID == 0) {

                    nextID = x.getInt("ID");
                }
            }
            //-----------------------------------------
            nextID++;
            if (erro == 0) {
                ps.executeUpdate("INSERT INTO EQUIPAMENTOS VALUES(" + idMAT + ",'" + tipoProduto + "','" + nomeProduto + "'," + preco + ",'" + descricao + "','" + username + "')");
                regist = 1;
                ps.executeUpdate("INSERT INTO HISTORICO VALUES(" + nextID + "," + idMAT + " ,'" + username + "' ,'Entregou')");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        ps.close();
        con.close();

        if (regist == 1) {
            atualizarEquipamentos(true);
            ultimosMove(username);
        }

    }

    /** ---------------------------------------------------------------------------------------------------------------------------------------------
     * Apagar uma linha da tabela
     * 
     * @param id
     * @param username
     * @throws SQLException
     * @throws IOException 
     */
    public void DeleteRow(int id, String username) throws SQLException, IOException {
        HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
        int i = 0;
        Connection con = DriverManager.getConnection("jdbc:derby://localhost:1527/Projeto", "app", "app");
        Statement ps = con.createStatement();
        int nextID = 0;
        int idmat = 0;
        try {
            ResultSet x = ps.executeQuery("SELECT * FROM HISTORICO");
            while (x.next()) {
                // check the int value

                if (nextID < x.getInt("ID") || nextID == 0) {

                    nextID = x.getInt("ID");
                }
            }
            //-----------------------------------------
            nextID++;
            //Verificar se o equipamento a apagar está alugado/reservado --------------------
            x = ps.executeQuery("SELECT COUNT(*) as count FROM EQUIPUSERS WHERE IDMAT = " + id + "");
            while (x.next()) {
                i = x.getInt("count");
            }
            if (i > 0) {
                FacesMessage fm = new FacesMessage("Impossivel remover um equipamento reservado/alugado!");
                FacesContext.getCurrentInstance().addMessage("msg", fm);
                return;
            }

            //--------------------------------------------------------------------
            PreparedStatement st = con.prepareStatement("DELETE FROM EQUIPAMENTOS WHERE IDMAT = " + id + "");
            st.executeUpdate();
            ps.executeUpdate("INSERT INTO HISTORICO VALUES(" + nextID + "," + id + " ,'" + username + "' ,'Removeu')");
            ultimosMove(username);
        } catch (Exception e) {
            System.out.println(e);
        }
        //Atualizar tabela de equipamentos
        atualizarEquipamentos(true);

    }

    /** ---------------------------------------------------------------------------------------------------------------------------------------------
     * Alugar/Reservar um equipamento
     * 
     * @param idMAT
     * @param username
     * @throws SQLException
     * @throws IOException 
     */
    public void alugarEquipamento(int idMAT, String username) throws SQLException, IOException {
        int i = 0, id = 0, idHistorico = 0;
        Connection con = DriverManager.getConnection("jdbc:derby://localhost:1527/Projeto", "app", "app");
        Statement ps1 = con.createStatement();
        ResultSet x = ps1.executeQuery("SELECT * FROM EQUIPUSERS WHERE IDMAT = " + idMAT);
        while (x.next()) {
            i++;
        }
        //Obter o ultimo ID da tabela equipUsers
        x = ps1.executeQuery("SELECT * FROM EQUIPUSERS");
        while (x.next()) {
            // check the int value

            if (id < x.getInt("ID") || id == 0) {

                id = x.getInt("ID");
            }
        }
        //-----------------------------------------
        x = ps1.executeQuery("SELECT * FROM HISTORICO");
        while (x.next()) {
            // check the int value

            if (idHistorico < x.getInt("ID") || idHistorico == 0) {

                idHistorico = x.getInt("ID");
            }
        }
        //-----------------------------------------
        id++;
        idHistorico++;
        try {
            ps1.executeUpdate("INSERT INTO EQUIPUSERS VALUES(" + id + "," + idMAT + " ,'" + username + "')");
            String tipo = "";
            if (i > 0) {
                tipo = "Reservou";
            } else {
                tipo = "Alugou";
                getcontacorrente(username);
            }
            ps1.executeUpdate("INSERT INTO HISTORICO VALUES(" + idHistorico + "," + idMAT + " ,'" + username + "' ,'" + tipo + "')");

        } catch (SQLException e) {
            e.printStackTrace();
        }
        //Se aquele material já está na tabela, é uma reserva
        if (i > 0) {
            FacesMessage fm = new FacesMessage("Reservado com sucesso!");
            FacesContext.getCurrentInstance().addMessage("msg", fm);
        } //Se é a primeira entrada do material na tabela, é um aluguel
        else {
            FacesMessage fm = new FacesMessage("Alugado com sucesso!");
            FacesContext.getCurrentInstance().addMessage("msg", fm);
        }
        ps1.close();
        con.close();
        ultimosMove(username);
    }

    /** ---------------------------------------------------------------------------------------------------------------------------------------------
     * Usado para if statements no xhtml 
     * 
     * @return 
     */
    public boolean existemReservados() {
        return (!equipamentosReservados.isEmpty());
    }

    public boolean existemTop3() {
        return (!listTop3.isEmpty());
    }

    public boolean existemNews() {
        return (!listNews.isEmpty());
    }

    public boolean existemMoves() {
        return (!historico.isEmpty());
    }

    public boolean existemAlugados() {
        return (!equipamentosAlugados.isEmpty());
    }
    // ---------------------------------------------------------------------------------------------------------------------------------------------

    /** ---------------------------------------------------------------------------------------------------------------------------------------------
     * Usado no if statements no xhtml -> verifica se o equipamento pertence ao user ativo
     * 
     * @param username
     * @param idmat
     * @return
     * @throws SQLException 
     */
    public boolean pertenceUser(String username, int idmat) throws SQLException {

        Connection con = DriverManager.getConnection("jdbc:derby://localhost:1527/Projeto", "app", "app");
        Statement ps1 = con.createStatement();
        ResultSet x = ps1.executeQuery("SELECT * FROM EQUIPAMENTOS WHERE IDMAT = " + idmat);

        while (x.next()) {
            // check the int value
            if (x.getString("USERNAME").equals(username)) {
                return true;
            }
        }
        return false;

    }

    /** ---------------------------------------------------------------------------------------------------------------------------------------------
     * Funçao que verifica se o utilizador: 1 - alugou 2 - reservou 3 - pode reservar 4 - pode alugar
     * 
     * @param idmat
     * @param username
     * @return 
     */
    public int getTextoBotao(int idmat, String username) {
        String user, minuser = "";
        boolean sameuser = false;
        int i = 0, id = 99999;
        Connection con = null;
        Statement ps = null;

        try {
            con = DriverManager.getConnection("jdbc:derby://localhost:1527/Projeto", "app", "app");
            ps = con.createStatement();
            ResultSet x = ps.executeQuery("SELECT * FROM EQUIPUSERS WHERE IDMAT = " + idmat);
            while (x.next()) {
                user = x.getString("USERNAME");

                if (x.getInt("ID") < id) {

                    id = x.getInt("ID");
                    minuser = user;
                }
                if (user.equals(username)) {
                    sameuser = true;
                }
                i++;
            }
            con.close();
            ps.close();
        } catch (SQLException ex) {
            Logger.getLogger(EquipamentoController.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (i > 0) {
            if (sameuser) {
                if (minuser.equals(username)) {
                    //devolver
                    return 1;
                } else {
                    //cancelar reserva
                    return 2;
                }
            } else {
                return 3;
            }
        } else {

            return 4;
        }
    }

    /** --------------------------------------------------------------------------------------------------------------------------------------------- 
     * Apresentar texto do botão consoante o estado obtido na função getTextoBotao
     * 
     * @param idmat
     * @param username
     * @return
     * @throws SQLException
     * @throws IOException 
     */
    public String apresentarTextoBotao(int idmat, String username) throws SQLException, IOException {
        int result = getTextoBotao(idmat, username);

        switch (result) {
            case 1:
                return "Devolver";
            case 2:
                return "Cancelar reserva";
            case 3:
                return "Reservar";
            default:
                break;
        }
        return "Alugar";
    }

    /** --------------------------------------------------------------------------------------------------------------------------------------------- 
     * Remove a entrada correspondente ao utilizador e material da tebela equipUsers
     * 
     * @param idmat
     * @param username
     * @param onProfile
     * @throws SQLException
     * @throws IOException 
     */
    public void cancelarReserva(int idmat, String username, boolean onProfile) throws SQLException, IOException {
        Connection connection = DriverManager.getConnection("jdbc:derby://localhost:1527/Projeto", "app", "app");
        Statement ps = connection.createStatement();
        int nextID = 0;
        int result = getTextoBotao(idmat, username);
        try {
            ResultSet x = ps.executeQuery("SELECT * FROM HISTORICO");
            while (x.next()) {
                // check the int value

                if (nextID < x.getInt("ID") || nextID == 0) {

                    nextID = x.getInt("ID");
                }
            }
            //-----------------------------------------
            nextID++;
            PreparedStatement st = connection.prepareStatement("DELETE FROM EQUIPUSERS WHERE IDMAT = ? and USERNAME=?");
            st.setInt(1, idmat);
            st.setString(2, username);
            st.executeUpdate();
        } catch (Exception e) {
            System.out.println(e);

        }

        if (result == 1) {
            FacesMessage fm = new FacesMessage("Equipamento entregue!");
            ps.executeUpdate("INSERT INTO HISTORICO VALUES(" + nextID + "," + idmat + " ,'" + username + "' ,'Devolveu')");
            FacesContext.getCurrentInstance().addMessage("msg", fm);
        } else if (result == 2) {
            FacesMessage fm = new FacesMessage("Reserva Cancelada!");
            ps.executeUpdate("INSERT INTO HISTORICO VALUES(" + nextID + "," + idmat + " ,'" + username + "' ,'Cancelou a Reserva')");
            FacesContext.getCurrentInstance().addMessage("msg", fm);
        }
        if (onProfile) {
            configurarEquipamentosUser(username, true);
        } else {
            atualizarEquipamentos(true);
        }
        ultimosMove(username);
    }

    /** --------------------------------------------------------------------------------------------------------------------------------------------- 
     * Biforca a ação do botao (se alugou/reservou então devolve/cancela, se nao alugou/reservou entao aluga/reserva)
     * 
     * @param idmat
     * @param username
     * @param onProfile
     * @throws SQLException
     * @throws IOException 
     */
    public void escolherbotao(int idmat, String username, boolean onProfile) throws SQLException, IOException {
        int result = getTextoBotao(idmat, username);

        if (result == 1 || result == 2) {
            //devolver
            cancelarReserva(idmat, username, onProfile);
        } else {
            //cancelar reserva
            alugarEquipamento(idmat, username);
        }
    }

    //GETTERS E SETTERS//
    public UsersController getUsersController() {
        return usersController;
    }

    public void setUsersController(UsersController usersController) {
        this.usersController = usersController;
    }

    public List<Equipamento> getEquipamentosReservados() {
        return equipamentosReservados;
    }

    public void setEquipamentosReservados(List<Equipamento> equipamentosReservados) {
        this.equipamentosReservados = equipamentosReservados;
    }

    public List<Equipamento> getEquipamentosAlugados() {
        return equipamentosAlugados;
    }

    public void setEquipamentosAlugados(List<Equipamento> equipamentosAlugados) {
        this.equipamentosAlugados = equipamentosAlugados;
    }

    public List<Equipamento> getEquipamentos() {
        return equipamentos;
    }

    public void setEquipamentos(List<Equipamento> equipamentos) {
        this.equipamentos = equipamentos;
    }

    public List<Integer> getIdEquipamentosUser() {
        return idEquipamentosUser;
    }

    public void setIdEquipamentosUser(List<Integer> idEquipamentosUser) {
        this.idEquipamentosUser = idEquipamentosUser;
    }

    public List<Integer> getIdAlugados() {
        return idAlugados;
    }

    public void setIdAlugados(List<Integer> idAlugados) {
        this.idAlugados = idAlugados;
    }

    public List<Integer> getIdReservados() {
        return idReservados;
    }

    public void setIdReservados(List<Integer> idReservados) {
        this.idReservados = idReservados;
    }

    public List<Integer> getIdhistorico() {
        return idhistorico;
    }

    public void setIdhistorico(List<Integer> idhistorico) {
        this.idhistorico = idhistorico;
    }

    public List<Historico> getHistorico() {
        List<Historico> story = new ArrayList<>();
        for (int i = historico.size() - 1; i >= 0; i--) {
            story.add(historico.get(i));
        }
        return story;
    }

    public void setHistorico(List<Historico> historico) {
        this.historico = historico;
    }

    public List<Integer> getIdtop3() {
        return idtop3;
    }

    public void setIdtop3(List<Integer> idtop3) {
        this.idtop3 = idtop3;
    }

    //----------------------//
    public List<Historico> getListTop3() {
        return listTop3;
    }

    public void setListTop3(List<Historico> listTop3) {
        this.listTop3 = listTop3;
    }

    public List<Integer> getIdNews() {
        return idNews;
    }

    public void setIdNews(List<Integer> idNews) {
        this.idNews = idNews;
    }

    public List<Equipamento> getListNews() {
        return listNews;
    }

    public void setListNews(List<Equipamento> listNews) {
        this.listNews = listNews;
    }

    public List<String> getTipoHistorico() {
        return tipoHistorico;
    }

    public void setTipoHistorico(List<String> tipoHistorico) {
        this.tipoHistorico = tipoHistorico;
    }

    public double getContacorrente() {
        return contacorrente;
    }

    public void setContacorrente(double contacorrente) {
        this.contacorrente = contacorrente;
    }

}
