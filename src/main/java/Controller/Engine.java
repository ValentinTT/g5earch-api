package Controller;

import Model.PostTerm;
import Model.Response;
import Model.VocabularyWord;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;

public class Engine {
    Connection connection;
    HashMap<String, VocabularyWord> vocabulary;
    int numberOfBooks; //N
    private static String splitRegex = "[,;:*\\s.\"¿?!¡{}\\[\\]\\(\\)]";

    public Engine(boolean shouldIndex) {
        connection = DBConnection.getConnection();
        index(shouldIndex);
        createVocabulary();
        countBooks();
    }

    public void index(boolean forceReindexing) {
        connection = DBConnection.getConnection();
        if (forceReindexing)
            deleteAllDB(); //remove db

        final File folder = new File("src/main/resources/static/documentos");

        for (final File fileEntry : folder.listFiles()) {
            System.out.println(fileEntry.getName());
            if (bookIsIndexed(fileEntry.getName())) continue;
            else postBook(fileEntry);

            //indexBook(fileEntry.getAbsolutePath(), fileEntry.getName());
            int bookId = getBookId(fileEntry.getName());
            indexBook(bookId, fileEntry.getPath());
            break;
        }
    }

    private int getBookId(String bookTitle) {
        try {
            ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM g5earch.g5earch.documentos WHERE titulo='" + bookTitle + "'");
            if(rs.next())
                return rs.getInt("ID");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private void postVocabulary(int bookId, HashMap<String, PostTerm> vocabulary) {
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
            //connection.prepareStatement(query.toString()).execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void postBook(File fileEntry) {
        try {
            String query = "INSERT INTO g5earch.g5earch.documentos (titulo, \"URI\") VALUES ('" + fileEntry.getName() + "', '" + fileEntry.getPath() + "');";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void indexBook(int bookId, String bookUri) { //TODO: delete bookName
        HashMap<String, PostTerm> vocabulary = new HashMap<>(100000);
        try {
            BufferedReader in = new BufferedReader(new FileReader(bookUri));
            String line;

            while ((line = in.readLine()) != null) {
                String[] words = line.split(splitRegex);
                for (String word : words) {
                    word = word.toLowerCase(Locale.ROOT);
                    PostTerm value = vocabulary.get(word);
                    if (value == null) { // First appearance in ALL indexing
                        vocabulary.put(word, new PostTerm(bookId, word));
                    } else {
                        value.increase();
                    }
                }
            }
            postVocabulary(bookId, vocabulary);
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
        //adding book into the folder and then indexig
        //indexBook(bookUri);
    }

    private void deleteAllDB() {
        try {
            connection.prepareStatement("DELETE FROM g5earch.g5earch.terminos").execute();
            connection.prepareStatement("DELETE FROM g5earch.g5earch.documentos").execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createVocabulary() {
        try {
            ResultSet rs = connection.prepareStatement("select nombre, count(*), max(frecuencia) from g5earch.terminos group by nombre;").executeQuery();
            String term;
            ArrayList<Integer> array;
            while (rs.next()) {
                term = rs.getString(1);
                vocabulary.put(term, new VocabularyWord(term, rs.getInt(2), rs.getInt(3)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void countBooks() {
        try {
            ResultSet rs = connection.prepareStatement("select count(*) from g5earch.documentos;").executeQuery();
            if (rs.next()) {
                numberOfBooks = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param searchQuery
     * @return an array of URIS to the most relevant documents
     */
    public ArrayList<String> search(String searchQuery, int numberOfResults) {
        String[] words = searchQuery.split(splitRegex);
        ArrayList<VocabularyWord> q = new ArrayList(words.length*2);

        for(String word: words) {
            VocabularyWord vocabularyWord = vocabulary.get(word);
            if(vocabularyWord != null) q.add(vocabularyWord);
        }

        Collections.sort(q);

        HashMap<String, Response> LD = new HashMap<>(); //"nombre doc" o ID, Response
        q.stream().forEach(vocabularyWord -> {
            //get numberOfResults documents for the word vocabularyWord
            //fijarse si están en LD, si está hacer response.updateRanking()
            //si no está crearlo con response(titulo, y un score) y en el futuro el uri y preview
            //el score lo sacaría de vocabulary.tf (la frec en todos los docs) y la frecuencia en este documento
        });

        // cuando termino las palabras ordeno por algo responses //TODO: implementar comparable en Response
        // devuelvo un subconjunto(numberOfResults) de LD
        return null;
    }
}

