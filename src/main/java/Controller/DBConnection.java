package Controller;

import Model.PostTerm;
import Model.VocabularyWord;

import java.io.File;
import java.sql.*;
import java.util.HashMap;

/**
 * @author Group 5 - DLC
 * @version 2022-June
 */

public class DBConnection {
    private Connection _connection;

    public DBConnection() {
        try {
            this._connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/g5earch", "postgres", "1234");
            this._connection.setSchema("g5earch");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void postVocabulary(HashMap<String, PostTerm> vocabulary) {
        try {
            StringBuilder query = new StringBuilder();
            query.append("INSERT INTO terms VALUES");
            vocabulary.forEach((k, v) ->
                    query.append(String.format(
                            "('%s',%d,%d),",
                            k.replace("'", "''"), v.getIdDoc(), v.getCount()
                    ))
            );
            query.replace(query.length() - 1, query.length(), ";");
            this._connection.prepareStatement(query.toString()).execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteAllDB() {
        try {
            this._connection.prepareStatement("DELETE FROM terms").execute();
            this._connection.prepareStatement("DELETE FROM documents").execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getBookId(String bookTitle) {
        try {
            PreparedStatement ps = this._connection.prepareStatement("SELECT * FROM documents WHERE title = ? ;");
            ps.setString(1, bookTitle);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getInt("ID");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void postBook(File fileEntry) {
        try {
            PreparedStatement ps = this._connection.prepareStatement("INSERT INTO documents (title, \"URI\") VALUES (?,?)");
            ps.setString(1, fileEntry.getName());
            ps.setString(2, fileEntry.getPath());
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return the amount of books indexed
     */
    public int countBooks() {
        try {
            ResultSet rs = this._connection.prepareStatement("select count(*) from documents").executeQuery();
            rs.next();
            return rs.getInt(1);
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
    public ResultSet getTerm(VocabularyWord term, int numberOfResults) {
        try {
            PreparedStatement ps = this._connection.prepareStatement("select documents.*, terms.frequency from terms join documents"
                    + " on documents.\"ID\" = terms.\"DocumentID\" where terms.name= ? order by terms.frequency desc limit ? ;");
            ps.setString(1, term.getWord());
            ps.setInt(2, numberOfResults);
            return ps.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @return all the columns of the table Terms
     */
    public ResultSet getAllTerms() {
        try {
            return this._connection.prepareStatement("select name, count(*), max(frequency) from terms group by name;").executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean fileExsists(String uri) {
        try {
            PreparedStatement ps = this._connection.prepareStatement("select 1 from documents where documents.\"URI\"= ?;");
            ps.setString(1, uri);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                if (rs.getInt(1) == 1) {
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