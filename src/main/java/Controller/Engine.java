package Controller;

import Model.PostTerm;
import Model.Response;
import Model.VocabularyWord;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class Engine {
    HashMap<String, VocabularyWord> vocabulary;
    int numberOfBooks; //N
    private static final String splitRegex = "[,;:*\\s.\"¿?!¡{}\\[\\]\\(\\)]";

    public Engine(boolean shouldIndex) {
        index(shouldIndex);
        vocabulary = DBConnection.createVocabulary();
        numberOfBooks = DBConnection.countBooks();
    }

    public void index(boolean forceReindexing) {
        if (forceReindexing)
            DBConnection.deleteAllDB(); //remove db

        final File folder = new File("src/main/resources/static/documentos");

        for (final File fileEntry : folder.listFiles()) {
            System.out.println(fileEntry.getName());
            if (bookIsIndexed(fileEntry.getName())) continue;
            else DBConnection.postBook(fileEntry);

            int bookId = DBConnection.getBookId(fileEntry.getName());
            indexBook(bookId, fileEntry.getPath());
            break;
        }
    }

    private void indexBook(int bookId, String bookUri) {
        HashMap<String, PostTerm> vocabulary = Reader.createBookVocabulary(bookId, bookUri, splitRegex);
        DBConnection.postVocabulary(vocabulary);
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

    /**
     * @param searchQuery
     * @param numberOfResults //R --> number of documents to return
     * @return
     */
    public ArrayList<String> search(String searchQuery, int numberOfResults) {
        String[] words = searchQuery.split(splitRegex);

        ArrayList<VocabularyWord> queryUser = new ArrayList(words.length * 2);

        for (String word : words) {
            //search for all words in our vocabulary
            VocabularyWord vocabularyWord = vocabulary.get(word);
            //add the word if its contained
            if (vocabularyWord != null) queryUser.add(vocabularyWord);
        }

        Collections.sort(queryUser);

        HashMap<String, Response> documentList = new HashMap<>(); //"nombre doc" o ID, Response
        queryUser.stream().forEach(vocabularyWord -> {

            //BUSCAR PARA CADA TERMINO (YA ESTAN ORDENADOS EN VOCABULARYWORD POR IDF):
            //      PARA TODOS LOS R DOCUMENTOS (O MENOS, VER ESTO), RECORRER DE MAYOR A MENOR TF
            //          DOCUMENTO NO ESTA EN LD
            //          {
            //              AGREGAR Y MANTENER LOS DOCUMENTOS ORDENADOS POR "IR" (tfk * idf)
            //          }
            //          SI ESTABA EN LD
            //          {
            //              SUBIR DE RANKING AL DOC DE ALGUNA FORMA.
            //          }

            //get numberOfResults documents for the word vocabularyWord
            //fijarse si están en documentsList, si está hacer response.updateRanking()
            //si no está crearlo con response(titulo, y un score) y en el futuro el uri y preview
            //el score lo sacaría de vocabulary.tf (la frec en todos los docs) y la frecuencia en este documento
        });

        // cuando termino las palabras ordeno por algo responses //TODO: implementar comparable en Response
        // devuelvo un subconjunto(numberOfResults) de documentsList
        return null;
    }
}

