package Controller;

import Model.PostTerm;
import Model.Response;
import Model.VocabularyWord;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class Engine {
    HashMap<String, VocabularyWord> vocabulary;
    int numberOfBooks; //N
    private static final String splitRegex = "[,;:*\\s.\"¿?!¡{}\\[\\]\\(\\)]"; //TODO: add spaces

    public Engine(boolean shouldIndex) {
        DBConnection.getConnection();
        index(shouldIndex);
        vocabulary = createVocabulary();
        numberOfBooks = DBConnection.countBooks();
    }

    private HashMap<String, VocabularyWord> createVocabulary() {
        ResultSet rs = DBConnection.getAllTerms();
        HashMap<String, VocabularyWord> vocabulary = new HashMap<>();
        String term;
        try {
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

    public void index(boolean forceReindexing) {
        if (forceReindexing)
            DBConnection.deleteAllDB(); //remove db

        final File folder = new File("src/main/resources/static/documentos");
        for (final File fileEntry : folder.listFiles()) {
            //TODO: calculate hashCode
            if (bookIsIndexed(fileEntry.getName())) continue;
            else DBConnection.postBook(fileEntry);

            int bookId = DBConnection.getBookId(fileEntry.getName());
            postVocabularyBook(bookId, fileEntry.getPath());
            //break; //TODO remove
        }
    }

    /**
     * Post the vocabulary for each book
     *
     * @param bookId
     * @param bookUri
     */
    private void postVocabularyBook(int bookId, String bookUri) {
        HashMap<String, PostTerm> vocabulary = Reader.createBookVocabulary(bookId, bookUri, splitRegex);
        DBConnection.postVocabulary(vocabulary);
    }

    /**
     * Indicate if a book is previously indexed
     *
     * @param bookTitle
     * @return
     */
    private boolean bookIsIndexed(String bookTitle) {
        int IDBook = DBConnection.getBookId(bookTitle);
        if(IDBook != -1){
            return true;
        }
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
    public List<Response> search(String searchQuery, int numberOfResults) {
        ArrayList<VocabularyWord> queryUser = buildQueryUser(searchQuery);
        HashMap<Integer, Response> documentList = new HashMap<>(); //HashMap<IDDoc, Response>

        queryUser.stream().forEach(vocabularyWord -> { //iterate over query words
            //increase for each document
            double increase = Math.log10(numberOfBooks / (double) (vocabularyWord.getNr()));
            ResultSet rsTerms = DBConnection.getTerm(vocabularyWord, numberOfResults);
            try {
                while (rsTerms.next()) {
                    int ID = rsTerms.getInt("ID");
                    //increase the relevance index, given the frequency (tf) of each term
                    double relevanceIndex = rsTerms.getInt("frequency") * increase;
                    if (documentList.get(ID) != null) { // update the response
                        documentList.get(ID).increaseScore(relevanceIndex);
                    } else {
                        //create response object
                        String name = rsTerms.getString("title");
                        String URI = rsTerms.getString("URI");
                        //TODO: definir que para los tf=0 no se lean y corte el ciclo
                        String preview = ""; //TODO: definir el preview
                        Response response = new Response(name, URI, preview, relevanceIndex);
                        documentList.put(ID, response);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        ArrayList<Response> result = new ArrayList<>(documentList.values());
        Collections.sort(result);
        return result.subList(0, numberOfResults);
    }

    private ArrayList<VocabularyWord> buildQueryUser(String searchQuery) {
        String[] words = searchQuery.split(splitRegex);

        ArrayList<VocabularyWord> queryUser = new ArrayList(words.length * 2);

        for (String word : words) {
            //search for all words in our vocabulary
            VocabularyWord vocabularyWord = vocabulary.get(word);
            //add the word if its contained
            if (vocabularyWord != null) queryUser.add(vocabularyWord);
        }

        //order by idf (less inverse frequencies) asc
        Collections.sort(queryUser);
        return queryUser;
    }

    public boolean fileExists(String URI) {
        return DBConnection.fileExsists(URI);
    }
}

