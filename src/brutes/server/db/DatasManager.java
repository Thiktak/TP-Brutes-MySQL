package brutes.server.db;

import brutes.server.ui;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

/**
 *
 * @author Olivares Georges <dev@olivares-georges.fr>
 */
public class DatasManager {

    private static final String XML_USERS = "res/users.xml";
    private static final String XML_TAG_USERS = "user";
    private static final String XML_TAG_USERS_LOGIN = "login";
    private static final String XML_TAG_USERS_PASSWORD = "password";
    private static final String XML_BRUTES = "res/brutes.xml";
    private static final String XML_TAG_BRUTES = "brute";
    private static final String XML_TAG_BRUTES_NAME = "name";
    private static final String XML_TAG_BRUTES_LEVEL = "level";
    private static final String XML_TAG_BRUTES_LIFE = "life";
    private static final String XML_TAG_BRUTES_STRENGTH = "strength";
    private static final String XML_TAG_BRUTES_SPEED = "speed";
    private static final String XML_TAG_BRUTES_IMAGE = "image";
    private static final String XML_TAG_BRUTES_BONUS = "bonus";
    private static final String XML_TAG_BRUTES_BONUS_ID = "id";
    private static final String XML_BONUS = "res/bonus.xml";
    private static final String XML_TAG_BONUS = "bonus";
    private static final String XML_TAG_BONUS_NAME = "name";
    private static final String XML_TAG_BONUS_LEVEL = "level";
    private static final String XML_TAG_BONUS_LIFE = "life";
    private static final String XML_TAG_BONUS_STRENGTH = "strength";
    private static final String XML_TAG_BONUS_SPEED = "speed";
    private static final String XML_TAG_BONUS_IMAGE = "image";
    static private Connection con;
    static public String type;

    public static Connection getInstance(String type, String dbpath, String pseudo, String password) throws IOException {
        Class classType;
        switch (type) {
            case "sqlite":
                DatasManager.type = type;
                try {
                    classType = Class.forName("org.sqlite.JDBC");
                    dbpath = "jdbc:sqlite:" + dbpath;
                } catch (ClassNotFoundException e) {
                    throw new IOException(e);
                }
                break;
            case "mysql":
                DatasManager.type = type;
                try {
                    classType = Class.forName("com.mysql.jdbc.Driver");
                    dbpath = "jdbc:mysql://" + dbpath;
                } catch (ClassNotFoundException e) {
                    throw new IOException(e);
                }
                break;
            default:
                throw new IOException(type + " SQL support not exists");
        }
        try {
            if (!pseudo.isEmpty()) {
                DatasManager.con = DriverManager.getConnection(dbpath, pseudo, password);
            } else {
                DatasManager.con = DriverManager.getConnection(dbpath);
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatasManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return DatasManager.con;
    }

    public static Connection getInstance(String type, String dbpath) throws IOException {
        return DatasManager.getInstance(type, dbpath, "", "");
    }

    public static void populate() throws IOException {
        try {
            Connection c = DatasManager.getInstance();

            DatasManager.executeUpdate("CREATE TABLE IF NOT EXISTS Users (id INTEGER PRIMARY KEY AUTOINCREMENT, pseudo TEXT, password TEXT, brute_id INTEGER, token TEXT, date_created DATETIME DEFAULT CURRENT_TIMESTAMP)");
            DatasManager.executeUpdate("CREATE TABLE IF NOT EXISTS Brutes (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, level INTEGER, life INTEGER, strength INTEGER, speed INTEGER, image_id INTEGER, date_created DATETIME DEFAULT CURRENT_TIMESTAMP)");
            DatasManager.executeUpdate("CREATE TABLE IF NOT EXISTS Bonus (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, level INTEGER, life INTEGER, strength INTEGER, speed INTEGER, image_id INTEGER)");
            DatasManager.executeUpdate("CREATE TABLE IF NOT EXISTS Shop (brute_id INTEGER, bonus_id INTEGER)");
            DatasManager.executeUpdate("CREATE TABLE IF NOT EXISTS Fights (id INTEGER PRIMARY KEY AUTOINCREMENT, brute_id1 INTEGER, brute_id2 INTEGER, winner_id INTEGER, date_created DATETIME DEFAULT CURRENT_TIMESTAMP)");

            SAXBuilder sxb = new SAXBuilder();
            PreparedStatement psql;

            Document xmlUsers = sxb.build(DatasManager.XML_USERS);
            Element rootUsers = xmlUsers.getRootElement();
            psql = DatasManager.prepare("INSERT INTO Users (pseudo, password) VALUES (?, ?)");
            for (Iterator<Element> i = rootUsers.getChildren(DatasManager.XML_TAG_USERS).iterator(); i.hasNext();) {
                Element current = i.next();
                psql.setString(1, current.getChild(DatasManager.XML_TAG_USERS_LOGIN).getText());
                psql.setString(2, current.getChild(DatasManager.XML_TAG_USERS_PASSWORD).getText());
                psql.executeUpdate();
            }

            Document xmlBonus = sxb.build(DatasManager.XML_BONUS);
            Element rootBonus = xmlBonus.getRootElement();
            psql = DatasManager.prepare("INSERT INTO Bonus (name, level, life, strength, speed, image_id) VALUES (?, ?, ?, ?, ?, ?)");
            for (Iterator<Element> i = rootBonus.getChildren(DatasManager.XML_TAG_BONUS).iterator(); i.hasNext();) {
                Element current = i.next();
                psql.setString(1, current.getChild(DatasManager.XML_TAG_BONUS_NAME).getText());
                psql.setInt(2, Integer.parseInt(current.getChild(DatasManager.XML_TAG_BONUS_LEVEL).getText()));
                psql.setInt(3, Integer.parseInt(current.getChild(DatasManager.XML_TAG_BONUS_LIFE).getText()));
                psql.setInt(4, Integer.parseInt(current.getChild(DatasManager.XML_TAG_BONUS_STRENGTH).getText()));
                psql.setInt(5, Integer.parseInt(current.getChild(DatasManager.XML_TAG_BONUS_SPEED).getText()));
                psql.setInt(6, Integer.parseInt(current.getChild(DatasManager.XML_TAG_BONUS_IMAGE).getText()));
                psql.executeUpdate();
            }

            Document xmlBrutes = sxb.build(DatasManager.XML_BRUTES);
            Element rootBrutes = xmlBrutes.getRootElement();
            psql = c.prepareStatement("INSERT INTO Brutes (name, level, life, strength, speed, image_id) VALUES (?, ?, ?, ?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);
            PreparedStatement psqlb = c.prepareStatement("INSERT INTO Shop (brute_id, bonus_id) VALUES (?, ?)");
            for (Iterator<Element> i = rootBrutes.getChildren(DatasManager.XML_TAG_BRUTES).iterator(); i.hasNext();) {
                Element current = i.next();
                psql.setString(1, current.getChild(DatasManager.XML_TAG_BRUTES_NAME).getText());
                psql.setInt(2, Integer.parseInt(current.getChild(DatasManager.XML_TAG_BRUTES_LEVEL).getText()));
                psql.setInt(3, Integer.parseInt(current.getChild(DatasManager.XML_TAG_BRUTES_LIFE).getText()));
                psql.setInt(4, Integer.parseInt(current.getChild(DatasManager.XML_TAG_BRUTES_STRENGTH).getText()));
                psql.setInt(5, Integer.parseInt(current.getChild(DatasManager.XML_TAG_BRUTES_SPEED).getText()));
                psql.setInt(6, Integer.parseInt(current.getChild(DatasManager.XML_TAG_BRUTES_IMAGE).getText()));
                psql.executeUpdate();
                ResultSet rs = psql.getGeneratedKeys();
                rs.next();
                int id = rs.getInt(1);
                Element bonus = current.getChild(DatasManager.XML_TAG_BRUTES_BONUS);
                if (bonus != null) {
                    for (Iterator<Element> ib = bonus.getChildren(DatasManager.XML_TAG_BRUTES_BONUS_ID).iterator(); ib.hasNext();) {
                        Element currentb = ib.next();
                        psqlb.setInt(1, id);
                        psqlb.setInt(2, Integer.parseInt(currentb.getText()));
                        psqlb.executeUpdate();
                    }
                }
            }
        } catch (JDOMException | SQLException ex) {
            Logger.getLogger(DatasManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static Connection getInstance() throws IOException {
        if (DatasManager.con == null) {
            throw new IOException("No instance of dataManager");
        }
        return DatasManager.con;
    }

    public static ResultSet exec(String query) throws IOException, SQLException {
        return DatasManager.getInstance().createStatement().executeQuery(query);
    }

    public static String queryPrepare(String query) {
        switch (DatasManager.type) {
            case "sqlite":
                query = query
                        .replaceAll("AUTO_INCREMENT", "AUTOINCREMENT")
                        .replaceAll("RAND\\(\\)", "RANDOM()");
                break;

            case "mysql":
                query = query
                        .replaceAll("AUTOINCREMENT", "AUTO_INCREMENT")
                        .replaceAll("DATETIME", "TIMESTAMP")
                        .replaceAll("RANDOM\\(\\)", "RAND()");
                break;
        }
        System.out.println("> " + query);
        return query;
    }

    public static PreparedStatement prepare(String query) throws IOException, SQLException {
        query = DatasManager.queryPrepare(query);
        return DatasManager.getInstance().prepareStatement(query);
    }

    private static PreparedStatement prepare(String query, int opt) throws IOException, SQLException {
        query = DatasManager.queryPrepare(query);
        return DatasManager.getInstance().prepareStatement(query, opt);
    }

    private static int executeUpdate(String query) throws IOException, SQLException {
        query = DatasManager.queryPrepare(query);
        return DatasManager.getInstance().createStatement().executeUpdate(query);
    }

    public static Statement getStatement() throws IOException, SQLException {
        return DatasManager.getInstance().createStatement();
    }

    public static <T> void save(T obj) throws IOException {
        try {
            Class classObj = Class.forName(ui.getClassPath(DatasManager.class) + ".entity." + obj.getClass().getSimpleName() + "Entity");

            Logger.getLogger(DatasManager.class.getName()).log(Level.INFO, "Call *.entity.{0}Entity::save", obj.getClass().getSimpleName());

            classObj.getMethod("save", new Class[]{Connection.class, obj.getClass()}).invoke(null, DatasManager.getInstance(), obj);

        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException | IllegalArgumentException ex) {
            Logger.getLogger(DatasManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static <T> T insert(T obj) throws IOException {
        try {
            Class classObj = Class.forName(ui.getClassPath(DatasManager.class) + ".entity." + obj.getClass().getSimpleName() + "Entity");

            Logger.getLogger(DatasManager.class.getName()).log(Level.INFO, "Call *.entity.{0}Entity::insert", obj.getClass().getSimpleName());

            return (T) classObj.getMethod("insert", new Class[]{Connection.class, obj.getClass()}).invoke(null, DatasManager.getInstance(), obj);

        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException | IllegalArgumentException ex) {
            Logger.getLogger(DatasManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static <T> void delete(T obj) throws IOException {
        try {
            Class classObj = Class.forName(ui.getClassPath(DatasManager.class) + ".entity." + obj.getClass().getSimpleName() + "Entity");

            Logger.getLogger(DatasManager.class.getName()).log(Level.INFO, "Call *.entity.{0}Entity::delete", obj.getClass().getSimpleName());

            classObj.getMethod("delete", new Class[]{Connection.class, obj.getClass()}).invoke(null, DatasManager.getInstance(), obj);

        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException | IllegalArgumentException ex) {
            Logger.getLogger(DatasManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
