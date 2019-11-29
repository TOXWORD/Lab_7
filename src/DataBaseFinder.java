import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class DataBaseFinder {

    private static final Logger logger = Logger.getLogger("log");

    static {
        try {
            LogManager.getLogManager().readConfiguration(new FileInputStream("logging.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException, IOException {

        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(Paths.get("database.properties"))) {
            props.load(in);
        }
        return DriverManager.getConnection(props.getProperty("url"), props.getProperty("username"), props.getProperty("password"));
    }

    public void createDataBase() throws IOException, SQLException {

        try (Connection c = getConnection()) {
            String sqlCommand = "CREATE TABLE DB (name VARCHAR(100), shortTitle VARCHAR(100), dateUpdate VARCHAR(100), " +
                    "address VARCHAR(100), dateFoundation VARCHAR(100), countEmployees INT, auditor VARCHAR(100), phone VARCHAR(100), " +
                    "email VARCHAR(100), branch VARCHAR(100), activity VARCHAR(100), link VARCHAR(100))";
            Statement statement = c.createStatement();
            statement.executeUpdate(sqlCommand);
            logger.info("Database has been created!");
        } catch (SQLSyntaxErrorException e) {
            logger.info(e.getMessage().toUpperCase());
        }
    }

    public void loadToDataBase() throws IOException, SQLException {

        try (Connection c = getConnection()) {
            try (Scanner sc = new Scanner(new File("input.csv"))) {

                while (sc.hasNextLine()) {
                    try (Scanner scWords = new Scanner(sc.nextLine()).useDelimiter(";")) {
                        String sqlCommand = "INSERT INTO DB (name, shortTitle, dateUpdate, " +
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
        logger.info("Data was loaded");
    }

    public void find(String request, int pos) throws IOException, SQLException {
        try (Connection c = getConnection()) {
            Statement statement = c.createStatement();
            ResultSet resultSet = statement.executeQuery(request);
            ResultSetMetaData rsmd = resultSet.getMetaData();
            JSONObject toRet = new JSONObject();
            JSONObject toAdd;

            int c_num = 0;

            try (PrintWriter pw1 = new PrintWriter("request" + pos + ".xml")) {
                pw1.println("<companies>");

                while (resultSet.next()) {
                    int i = 1;
                    toAdd = new JSONObject();
                    pw1.println("<company_" + c_num + ">");

                    try {
                        while (true) {
                            toAdd.put(rsmd.getColumnName(i), resultSet.getString(i));
                            pw1.println("<" + rsmd.getColumnName(i) + ">" + resultSet.getString(i) + "</" + rsmd.getColumnName(i) + ">");
                            i++;
                        }
                    } catch (Exception e) {
                    }

                    toRet.put("company_" + c_num, toAdd);
                    pw1.println("</company_" + c_num + ">");
                    c_num++;
                }
                pw1.println("</companies>");

                try (PrintWriter pw = new PrintWriter("request" + pos + ".json")) {
                    pw.println(toRet);
                }
            }
            logger.info(c_num + " companies were found by the request: " + request);
        }
    }

    public void dropDataBase() throws IOException, SQLException {
        try (Connection c = getConnection()) {
            Statement st = c.createStatement();
            st.executeUpdate("DROP TABLE IF EXISTS DB");
        }
        logger.info("Database was deleted");
    }

}
