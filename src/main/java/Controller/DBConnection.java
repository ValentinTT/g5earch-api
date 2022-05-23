package Controller;

import Model.PostTerm;
import Model.VocabularyWord;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class DBConnection {
    private static Connection _connection;

    private static void createConnection() {
        try {
            _connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/g5earch", "postgres", "1234");
            _connection.setSchema("g5earch"); //TODO: chequear
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        if (_connection == null) {
            createConnection();
        }
        return _connection;
    }

    public static void postVocabulary(HashMap<String, PostTerm> vocabulary) {
        try {
            StringBuilder query = new StringBuilder();
            query.append("INSERT INTO g5earch.g5earch.terms VALUES");
            vocabulary.forEach((k, v) ->
                    query.append(String.format(
                            "('%s',%d,%d),",
                            k.replace("'", "''"), v.getIdDoc(), v.getCount()
                    ))
            );
            query.replace(query.length() - 1, query.length(), ";");
            _connection.prepareStatement(query.toString()). execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteAllDB() {
        try {
            _connection.prepareStatement("DELETE FROM g5earch.g5earch.terms").execute();
            _connection.prepareStatement("DELETE FROM g5earch.g5earch.documents").execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static int getBookId(String bookTitle) {
        try {
            ResultSet rs = _connection.createStatement().executeQuery("SELECT * FROM g5earch.g5earch.documents WHERE title='" + bookTitle + "'");
            if (rs.next())
                return rs.getInt("ID");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static void postBook(File fileEntry) {
        try {
            //TODO calcular el hascode del libro
            String query = "INSERT INTO g5earch.g5earch.documents (title, \"URI\") VALUES ('" +
                    fileEntry.getName() + "', '" + fileEntry.getPath() + "');";
            _connection.prepareStatement(query).execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return the amount of books indexed
     */
    public static int countBooks() {
        try {
            ResultSet rs = _connection.prepareStatement("select count(*) from g5earch.documents").executeQuery();
            rs.next();
            rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * @param term
     * @param numberOfResults
     * @return all the columns of the table Documents and the frequency of the term
     */
    public static ResultSet getTerm(VocabularyWord term, int numberOfResults) {
        try {
            return _connection.prepareStatement("select documents.*, terms.frequency from g5earch.terms join g5earch.documents"
                    + " on documents.\"ID\" = terms.\"DocumentID\" where terms.name='" + term.getWord() +
                    "' order by terms.frequency desc limit " + numberOfResults + ";").executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @return all the columns of the table Terms
     */
    public static ResultSet getAllTerms() {
        try {
            return _connection.prepareStatement("select name, count(*), max(frequency) from g5earch.terms group by name;").executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean fileExsists(String uri) {
        try {
            ResultSet rs = _connection.prepareStatement("select 1 from g5earch.documents where documents.\"URI\"=" + uri + ";").executeQuery();
            if(rs.next()){
                if(rs.getInt(1) == 1){
                    return true;
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
        return false;
    }
}