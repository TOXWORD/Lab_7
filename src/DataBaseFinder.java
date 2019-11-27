import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Properties;
import java.util.Scanner;

public class DataBaseFinder {

    public static Connection getConnection() throws SQLException, IOException {

        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(Paths.get("database.properties"))) {
            props.load(in);
        }
        String url = props.getProperty("url");
        String username = props.getProperty("username");
        String password = props.getProperty("password");

        return DriverManager.getConnection(url, username, password);
    }

    public void createDataBase() throws IOException, SQLException {

        try (Connection c = getConnection()) {
            String sqlCommand = "CREATE TABLE db (name VARCHAR(100), shortTitle VARCHAR(100), dateUpdate VARCHAR(100), " +
                    "address VARCHAR(100), dateFoundation VARCHAR(100), countEmployees INT, auditor VARCHAR(100), phone VARCHAR(100), " +
                    "email VARCHAR(100), branch VARCHAR(100), activity VARCHAR(100), link VARCHAR(100))";
            Statement statement = c.createStatement();
            statement.executeUpdate(sqlCommand);
            System.out.println("Database has been created!");
        } catch (SQLSyntaxErrorException e) {
            System.out.println(e.getMessage().toUpperCase());
        }
    }

    public void loadToDataBase() throws IOException, SQLException {

        try (Connection c = getConnection()) {
            try (Scanner sc = new Scanner(new File("input.csv"))) {

                while (sc.hasNextLine()) {
                    try (Scanner scWords = new Scanner(sc.nextLine()).useDelimiter(";")) {
                        String sqlCommand = "INSERT INTO db (name, shortTitle, dateUpdate, " +
                                "address, dateFoundation, countEmployees, auditor, phone, " +
                                "email, branch, activity, link) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
                        PreparedStatement preparedStatement = c.prepareStatement(sqlCommand);
                        preparedStatement.setString(1, scWords.next());
                        preparedStatement.setString(2, scWords.next());
                        preparedStatement.setString(3, scWords.next());
                        preparedStatement.setString(4, scWords.next());
                        preparedStatement.setString(5, scWords.next());
                        preparedStatement.setInt(6, Integer.parseInt(scWords.next()));
                        preparedStatement.setString(7, scWords.next());
                        preparedStatement.setString(8, scWords.next());
                        preparedStatement.setString(9, scWords.next());
                        preparedStatement.setString(10, scWords.next());
                        preparedStatement.setString(11, scWords.next());
                        preparedStatement.setString(12, scWords.next());

                        preparedStatement.executeUpdate();
                    }
                }
            }
        }
    }

    public void find(String request, int pos) throws IOException, SQLException {
        try (Connection c = getConnection()) {
            Statement statement = c.createStatement();
            ResultSet resultSet = statement.executeQuery(request);
            ResultSetMetaData rsmd = resultSet.getMetaData();
            JSONObject toRet = new JSONObject();
            JSONObject toAdd;

            int k = 0;

            while (resultSet.next()) {
                int i = 1;
                toAdd = new JSONObject();
                try {
                    while (true) {
                        toAdd.put(rsmd.getColumnName(i), resultSet.getString(i));
                        i++;
                    }
                } catch (Exception e) { }
                toRet.put("company_" + k, toAdd);
                k++;
            }
            try (PrintWriter pw = new PrintWriter("request" + pos + ".json")) {
                pw.println(toRet);
            }
        }
    }

    public void dropDataBase() throws IOException, SQLException {
        try (Connection c = getConnection()) {
            Statement st = c.createStatement();
            st.executeUpdate("DROP TABLE IF EXISTS db");
        }
    }

}
