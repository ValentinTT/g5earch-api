package Controller;

import Model.PostTerm;
import Model.Response;
import Model.VocabularyWord;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
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
    private static final String splitRegex = "[,;:*\\s.\"¿?!¡{}\\[\\]\\(\\)]";

    public Engine(boolean shouldIndex) {
        connection = DBConnection.getConnection();
        index(shouldIndex);
        vocabulary = DBConnection.createVocabulary();
        countBooks();
    }

    public void index(boolean forceReindexing) {
        connection = DBConnection.getConnection();
        if (forceReindexing)
            DBConnection.deleteAllDB(); //remove db

        final File folder = new File("src/main/resources/static/documentos");

        for (final File fileEntry : folder.listFiles()) {
            System.out.println(fileEntry.getName());
            if (bookIsIndexed(fileEntry.getName())) continue;
            else DBConnection.postBook(fileEntry);

            //indexBook(fileEntry.getAbsolutePath(), fileEntry.getName());
            int bookId = DBConnection.getBookId(fileEntry.getName());
            indexBook(bookId, fileEntry.getPath());
            break;
        }
    }

    private void postVocabulary(HashMap<String, PostTerm> vocabulary) {
        // Add book to document's tablet
        DBConnection.postVocabulary(vocabulary);
    }

    private void indexBook(int bookId, String bookUri) {
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
            postVocabulary(vocabulary);
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
        //adding book into the folder and then indexig
        //indexBook(bookUri);
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
        ArrayList<VocabularyWord> q = new ArrayList(words.length * 2);

        for (String word : words) {
            VocabularyWord vocabularyWord = vocabulary.get(word);
            if (vocabularyWord != null) q.add(vocabularyWord);
        }

        Collections.sort(q);

        HashMap<String, Response> documentList = new HashMap<>(); //"nombre doc" o ID, Response
        q.stream().forEach(vocabularyWord -> {
            //BUSCAR PARA CADA TERMINO (YA ESTAN ORDENADOS EN VOCABULARYWORD POR IDF), LOS R DOCUMENTOS (SI TIENE) DE MAYOR A MENOR TF.
            //MANTENER LOS DOCUMENTOS ORDENADOS EN EL ORDEN EN EL QUE INGRESAN. SI UN DOCUMENTO APARECE MAS DE UNA VEZ (TERMINOS DISTINTOS)
            //SUBIR DE RANKING AL DOC.
            //

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

