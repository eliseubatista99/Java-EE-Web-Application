package Controller;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.ejb.Stateful;
import java.sql.DriverManager;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

@Stateful
@ManagedBean(name = "user")
@SessionScoped
public class UsersController implements Serializable {

    private String username;
    private String password;
    private String gen;
    private boolean haslogged = false;
    private boolean toRedirect = false;
    private String redirectPage;

    public String getGen() {
        return gen;
    }

    public void setGen(String gen) {
        this.gen = gen;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void checkLogin() {
        HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();

        if (username == null) {
            try {
                toRedirect = true;
                FacesContext facesContext = FacesContext.getCurrentInstance();
                String viewId = facesContext.getViewRoot().getViewId();
                System.out.print(viewId); //will print your current path URL
                String pageName = null;
                String[] parts = viewId.split("/");
                for (int i = 0; i < parts.length; i++) {
                    pageName = parts[i];
                }
                redirectPage = pageName;
                System.out.print("PÁGINA == " + pageName); //will print your current path URL
                response.sendRedirect("entrar.xhtml");
            } catch (IOException ex) {
                Logger.getLogger(UsersController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void login() throws SQLException, IOException {

        // integer var
        int amount = 0;
        HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();

        // connection to the database
        Connection con = DriverManager.getConnection("jdbc:derby://localhost:1527/Projeto", "app", "app");
        // prepare to create a statement
        Statement ps = con.createStatement();

        try {
            // verifica se existe na tabela users algum com o username e password
            ResultSet x = ps.executeQuery("SELECT COUNT(*) as count FROM USERS WHERE username = '" + username + "' and password = '" + password + "'");
            // returns in 'list' form
            while (x.next()) {
                // check the int value
                amount = x.getInt("count");
            }
        } catch (SQLException e) {
        };

        // close the connection and the statement
        con.close();
        ps.close();

        // **if there's** a user direct to the new page "home.xhtml" *else* nothing happens
        if (amount == 1) {
            if (toRedirect) {
                response.sendRedirect(redirectPage);
            } else {
                response.sendRedirect("home.xhtml");

            }

            toRedirect = false;
            redirectPage = null;
            
            
        } else {

            FacesMessage fm = new FacesMessage("Username ou Password inválidas");
            FacesContext.getCurrentInstance().addMessage("msg", fm);

        }
    }

    public void logout() throws IOException {
        HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();

        this.username = "";
        this.password = "";
        this.gen = null;

        response.sendRedirect("index.xhtml");

    }

    public void Registo(String username, String password, String password2, String email, String pnome, String unome, String phone) throws SQLException, IOException {
        int erro = 0, register = 0, num = 0;
        HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
        // connection to the database
        Connection con = DriverManager.getConnection("jdbc:derby://localhost:1527/Projeto", "app", "app");
        // prepare to create a statement
        Statement ps = con.createStatement();

        if (username.equals("")) {
            erro = 1;
            FacesMessage fm = new FacesMessage("Campo username em branco!");
            FacesContext.getCurrentInstance().addMessage("msg", fm);
        }

        try {
            // verifica se existe na tabela users algum com o username e password
            ResultSet x = ps.executeQuery("SELECT COUNT(*) as count FROM USERS WHERE username = '" + username + "'");
            // returns in 'list' form
            while (x.next()) {
                // check the int value
                num = x.getInt("count");
            }
        } catch (SQLException e) {
        }

        if (num > 0) {
            erro = 1;
            FacesMessage fm = new FacesMessage("Nome de Utilizador já registado!");
            FacesContext.getCurrentInstance().addMessage("msg", fm);

        }

        // if the passwords are not the same
        if (!password.equals(password2)) {
            erro = 1;
            FacesMessage fm = new FacesMessage("Passwords não são iguais!");
            FacesContext.getCurrentInstance().addMessage("msg", fm);
        }

        if (password.equals("")) {
            erro = 1;
            FacesMessage fm = new FacesMessage("Campo password em branco!");
            FacesContext.getCurrentInstance().addMessage("msg", fm);

        }

        if (pnome.equals("")) {
            erro = 1;
            FacesMessage fm = new FacesMessage("Campo primeiro nome em branco!");
            FacesContext.getCurrentInstance().addMessage("msg", fm);

        }

        if (unome.equals("")) {
            erro = 1;
            FacesMessage fm = new FacesMessage("Campo último nome em branco!");
            FacesContext.getCurrentInstance().addMessage("msg", fm);

        }

        if (email.equals("")) {

            erro = 1;
            FacesMessage fm = new FacesMessage("Campo email em branco!");
            FacesContext.getCurrentInstance().addMessage("msg", fm);
        }

        if (gen == null) {

            erro = 1;
            FacesMessage fm = new FacesMessage("Campo género em branco!");
            FacesContext.getCurrentInstance().addMessage("msg", fm);

        }

        if (phone.equals("")) {

            erro = 1;
            FacesMessage fm = new FacesMessage("Campo número de telefone em branco!");
            FacesContext.getCurrentInstance().addMessage("msg", fm);

        }

        if (erro != 1) {
            try {
                //insert in the database Users (username, password)
                ps.executeUpdate("INSERT INTO USERS VALUES('" + username + "','" + password + "','" + gen + "','" + email + "','" + pnome + "','" + unome + "','" + phone + "')");
                register = 1;
            } catch (SQLException e) {
            }
        }
        // close the connection and the statement
        ps.close();
        con.close();

        if (register == 1) {
            response.sendRedirect("index.xhtml");
        }

    }
    // ---------------------------------------------------------------------------------------------------------------------------------------------------

}
