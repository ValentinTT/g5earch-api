package Controller;

public class DBConnection {
    private static DBConnection _connection;

    public static DBConnection createConnection() {
        if(_connection == null) {
            return new DBConnection();
        }
        return _connection;
    }

    private DBConnection() {

    }
}
