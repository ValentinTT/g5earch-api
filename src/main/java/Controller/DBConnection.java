package Controller;

import Model.PostTerm;
import Model.VocabularyWord;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class DBConnection {
    private static Connection _connection;

    private static void createConnection() {
        try {
            _connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/g5earch", "postgres", "datatalks");
            _connection.setSchema("g5earch"); //TODO: chequear
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        if (_connection == null) {
            createConnection()
        }
        return _connection;
    }

    public static void postVocabulary(HashMap<String, PostTerm> vocabulary) {
        try {
            StringBuilder query = new StringBuilder();
            query.append("INSERT INTO g5earch.g5earch.terminos VALUES");
            vocabulary.forEach((k, v) ->
                    query.append(String.format(
                            "('%s',%d,%d),",
                            k.replace("'", "''"), v.getIdDoc(), v.getCount()
                    ))
            );
            query.deleteCharAt(query.length() - 1);
            query.append(";");
            _connection.prepareStatement(query.toString()).execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteAllDB() {
        try {
            _connection.prepareStatement("DELETE FROM g5earch.g5earch.terminos").execute();
            _connection.prepareStatement("DELETE FROM g5earch.g5earch.documentos").execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static int getBookId(String bookTitle) {
        try {
            ResultSet rs = _connection.createStatement().executeQuery("SELECT * FROM g5earch.g5earch.documentos WHERE titulo='" + bookTitle + "'");
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
            String query = "INSERT INTO g5earch.g5earch.documentos (titulo, \"URI\") VALUES ('" + fileEntry.getName() + "', '" + fileEntry.getPath() + "');";
            PreparedStatement ps = _connection.prepareStatement(query);
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static HashMap<String, VocabularyWord> createVocabulary() {
        try {
            HashMap<String, VocabularyWord> vocabulary = new HashMap<String, VocabularyWord>();
            ResultSet rs = _connection.prepareStatement("select nombre, count(*), max(frecuencia) from g5earch.terminos group by nombre;").executeQuery();
            String term;
            ArrayList<Integer> array;
            while (rs.next()) {
                term = rs.getString(1);
                vocabulary.put(term, new VocabularyWord(term, rs.getInt(2), rs.getInt(3)));
            }
            return vocabulary;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int countBooks() {
        try {
            return _connection.prepareStatement("select count(*) from g5earch.documentos").executeQuery().getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public ResultSet getTermPosted(VocabularyWord term){
        try {
            return _connection.prepareStatement("select documentos.*, terminos.frecuencia from g5earch.terminos join g5earch.documentos" +
                    "on documentos.\"ID\" = terminos.\"IDDocumento\" where terminos.nombre='" + term.getWord() +
                    "' order by terminos.frecuencia desc;").executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}