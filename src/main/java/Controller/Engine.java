package Controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;

public class Engine {
    // nr tf aux
    HashMap<String, ArrayList<Integer>> vocabulary;
    Connection connection;

    public Engine() {
        vocabulary = new HashMap<>();
        connection = DBConnection.getConnection();
    }

    public void index(boolean force) {
        // if(force) //remove db
        final File folder = new File("src/main/resources/static/documentos");
        String query = "INSERT INTO g5earch.g5earch.documentos (titulo, \"URI\") VALUES ";

        for (final File fileEntry : folder.listFiles()) {
            System.out.println(fileEntry.getName());

            if (bookIsIndexed(fileEntry.getName()))
                continue;
            else { // Agrego el documento nuevo si no está indexado
                query += "('" + fileEntry.getName() + "', '" + fileEntry.getPath() + "'),";
            }
            // Indexar documento actual
            // indexBook(fileEntry.getAbsolutePath());
        }
        // Termino de armar la query para insertar
        query = query.substring(0, query.length() - 1);
        query += ";";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        long endTime = System.nanoTime();
        System.out.println((endTime - startTime) / 1000000000);
        // print vocabulary
        // printVocab();
    }

    // TODO: remove
    private void printVocab() {
        vocabulary.forEach((k, v) -> System.out.println(k + "->" + v.toString()));
    }

    private void clearVocabulary() {
        // clear Vocabulary 3 position. TODO: capaz funcional es menos eficiente
        vocabulary.forEach((k, v) -> v.set(2, 0));
    }

    private void postVocabulary(String bookTitle) {
        // Add book to document's tablet
        // Add each vocabulary entry with index 2 != 0 to the table post
        try {
            ResultSet rs = connection.createStatement()
                    .executeQuery("SELECT * FROM g5earch.g5earch.documentos WHERE titulo='" + bookTitle + "'");
            rs.next();
            final int bookId = rs.getInt("ID");
            vocabulary.forEach((k, v) -> {
                if (v.get(2) == 0)
                    return;
                // insert into post table
                try {
                    String query = "INSERT INTO g5earch.g5earch.terminos VALUES ('" + k.replace("'", "''") + "',"
                            + bookId + "," + v.get(2) + ");";
                    connection.prepareStatement(query).execute();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void postBook(File fileEntry) {
        try {
            String query = "INSERT INTO g5earch.g5earch.documentos (titulo, \"URI\") VALUES ('" + fileEntry.getName()
                    + "', '" + fileEntry.getPath() + "');";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void indexBook(String bookUri, String bookName) { // TODO: delete bookName
        clearVocabulary();
        try {
            BufferedReader in = new BufferedReader(new FileReader(bookUri));
            String line;

            while ((line = in.readLine()) != null) {
                String[] words = line.split("[,;:*\\s.\"¿?!¡{}\\[\\]\\(\\)]");
                for (String word : words) {
                    word = word.toLowerCase(Locale.ROOT);
                    ArrayList<Integer> value = vocabulary.get(word);
                    if (value == null) { // First appearance in ALL indexing
                        value = new ArrayList(Arrays.asList(1, 1, 1)); // Initialize aux
                        vocabulary.put(word, value);
                    } else {
                        if (value.get(2) == 0) // First word's appearance in this document, increase n
                            value.set(0, value.get(0) + 1);
                        value.set(2, value.get(2) + 1); // increase aux
                        // update maxTf
                        value.set(1, Math.max(value.get(2), value.get(1)));
                    }
                }
            }
            postVocabulary(bookName);
            in.close();
        } catch (IOException e) {
            System.out.println("File Read Error");
        }
    }

    /**
     * Indica si existe un libro con el id de bookName
     *
     * @param bookName
     * @return
     */
    private boolean bookIsIndexed(String bookName) {

        return false;
    }

    public void addBook() {
        // adding book into the folder and then indexig
        // indexBook(bookUri);
    }

    private void deleteAllDB() {
        try {
            connection.prepareStatement("DELETE FROM g5earch.g5earch.terminos").execute();
            connection.prepareStatement("DELETE FROM g5earch.g5earch.documentos").execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
