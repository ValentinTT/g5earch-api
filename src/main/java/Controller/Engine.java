package Controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
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
    HashMap<String, ArrayList<Integer>> frequencyTable;
    int numberOfBooks; //N

    public Engine(boolean shouldIndex) {
        vocabulary = new HashMap<>();
        connection = DBConnection.getConnection();
        index(shouldIndex);
        createFrequencyTable();
        countBooks();
    }

    public void index(boolean force) {
        if (force)
            deleteAllDB();

        final File folder = new File("src/main/resources/static/documentos");
        long startTime = System.nanoTime();
        for (final File fileEntry : folder.listFiles()) {
            if (bookIsIndexed(fileEntry.getName()))
                continue;
            else { // Agrego el documento nuevo si no está indexado
                postBook(fileEntry);
            }
            // Indexar documento actual
            indexBook(fileEntry.getPath());
            break;
        }
        long endTime = System.nanoTime();
        System.out.println((endTime - startTime) / 1000000000);
        // print vocabulary
        //printVocab();
    }

    // TODO: remove
    private void printVocab() {
        vocabulary.forEach((k, v) -> System.out.println(k + "->" + v.toString()));
    }

    private void clearVocabulary() {
        // clear Vocabulary 3 position. TODO: capaz funcional es menos eficiente
        vocabulary.forEach((k, v) -> v.set(2, 0));
    }

    private void postVocabulary(String bookURI) {
        // Add book to document's tablet
        // Add each vocabulary entry with index 2 != 0 to the table post
        try {
            ResultSet rs = connection.createStatement()
                    .executeQuery("SELECT * FROM g5earch.g5earch.documentos WHERE \"URI\"= '" + bookURI + "'");
            rs.next();
            final int bookId = rs.getInt("ID");
            StringBuilder query = new StringBuilder();
            query.append("INSERT INTO g5earch.g5earch.terminos VALUES");
            vocabulary.entrySet().stream().filter(e -> e.getValue().get(2) != 0)
                    .forEach((e) -> {
                        query.append(String.format("('%s',%d,%d),", e.getKey().replace("'", "''"), bookId, e.getValue().get(2)));
                        System.out.println(String.format("('%s',%d,%d),", e.getKey().replace("'", "''"), bookId, e.getValue().get(2)));
                    });
            query.deleteCharAt(query.length() - 1);
            query.append(";");

            //connection.prepareStatement(query.toString()).execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void postBook(File fileEntry) {
        try {
            String query = "INSERT INTO g5earch.g5earch.documentos (titulo, \"URI\") VALUES ('" + fileEntry.getName()
                    + "', '" + fileEntry.getPath() + "');";
            connection.prepareStatement(query).execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void indexBook(String bookUri) {
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
            postVocabulary(bookUri);
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
        //TODO
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

    public void createFrequencyTable() {
        try {
            ResultSet rs = connection.prepareStatement("select nombre, count(*), max(frecuencia) from g5earch.terminos group by nombre;").executeQuery();
            String term;
            ArrayList<Integer> array;
            while (rs.next()) {
                term = rs.getString(1);
                array = new ArrayList<>(Arrays.asList(rs.getInt(2), rs.getInt(3)));
                frequencyTable.put(term, array);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void countBooks() {
        try {
            ResultSet rs = connection.prepareStatement("select count(*) from g5earch.documentos;").executeQuery();
            while (rs.next()) {
                numberOfBooks = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}