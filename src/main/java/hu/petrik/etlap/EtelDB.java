package hu.petrik.etlap;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EtelDB {
    private Connection conn;
    public static String DB_DRIVER = "mysql";
    public static String DB_HOST = "localhost";
    public static String DB_PORT = "3306";
    public static String DB_NAME = "etlapdb";
    public static String DB_USERNAME = "root";
    public static String DB_PASSWORD = "";

    public EtelDB() throws SQLException {
        String url = String.format("jdbc:%s://%s:%s/%s", DB_DRIVER, DB_HOST, DB_PORT, DB_NAME);
        conn = DriverManager.getConnection(url, DB_USERNAME, DB_PASSWORD);
    }

    public boolean createEtel(Etel etel) throws SQLException {
        String sql = "INSERT INTO etlap(nev, leiras, ar, kategoria) VALUES (?, ?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, etel.getNev());
        stmt.setString(2, etel.getLeiras());
        stmt.setInt(3, etel.getAr());
        stmt.setString(4, etel.getKategoria());
        return stmt.executeUpdate() > 0;
    }

    public List<Etel> readEtelek() throws SQLException {
        List<Etel> etelek = new ArrayList<>();
        String sql = "SELECT * FROM etlap";
        Statement stmt = conn.createStatement();
        ResultSet result = stmt.executeQuery(sql);
        while(result.next()) {
            int id = result.getInt("id");
            String nev = result.getString("nev");
            String leiras = result.getString("leiras");
            int ar = result.getInt("ar");
            String kategoria = result.getString("kategoria");
            Etel etel = new Etel(id, nev, leiras, ar, kategoria);
            etelek.add(etel);
        }
        return etelek;
    }

    public boolean updateEtel(Etel etel) throws SQLException {
        String sql = "UPDATE etlap SET nev = ?, leiras = ?, ar= ?, kategoria= ? WHERE id = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, etel.getNev());
        stmt.setString(2, etel.getLeiras());
        stmt.setInt(3, etel.getAr());
        stmt.setString(4, etel.getKategoria());
        stmt.setInt(5, etel.getId());
        return stmt.executeUpdate() > 0;
    }

    public boolean deleteEtel(int id) throws SQLException {
        String sql = "DELETE FROM etlap WHERE id = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, id);
        return stmt.executeUpdate() > 0;
    }
}
