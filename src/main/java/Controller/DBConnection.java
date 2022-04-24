package Controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static Connection _connection;

    private static void createConnection() {
        try {
            Connection _connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/g5earch", "postgres", "1234");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection(){
        if(_connection == null) {
            createConnection();
        }
        return _connection;
    }

    private DBConnection() {

    }
}
