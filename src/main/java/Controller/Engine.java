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

/**
 * @author Group 5 - DLC
 * @version 2022-June
 */

public class Engine {
    DBConnection dbConnection;
    HashMap<String, VocabularyWord> vocabulary;
    int numberOfBooks; //N
    String FILE_DIRECTORY;
    private static final String splitRegex = "[,;:*\\-=^<>+@|\\/\\\\_#$%&¬|~`¨´'°\\s.\"¿?!¡{}\\[\\]\\(\\)]";

    /**
     * @param shouldIndex
     * @param FILE_DIRECTORY
     */
    public Engine(boolean shouldIndex, String FILE_DIRECTORY) {
        dbConnection = new DBConnection();
        this.FILE_DIRECTORY = FILE_DIRECTORY;
        index(shouldIndex);
        vocabulary = createVocabulary();
        numberOfBooks = dbConnection.countBooks();
    }

    /**
     * Creates the vocabulary in memory for all the terms indexed, creating the VocabylaryWord objects.
     *
     * @return a HashMap where:
     * Key: is the term
     * Value: is an object of the class VocabularyWord
     */
    private HashMap<String, VocabularyWord> createVocabulary() {
        ResultSet rs = dbConnection.getAllTerms();
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

    /**
     * Indexes the documents and terms, and post them on the database
     *
     * @param forceReindexing if it's true: force to index all the documents again, deleting all the documents and terms previously indexed
     *                        if it's false: just index the documents that are not indexed
     */
    public void index(boolean forceReindexing) {
        if (forceReindexing)
            dbConnection.deleteAllDB();

        final File folder = new File(this.FILE_DIRECTORY);
        for (final File fileEntry : folder.listFiles()) {
            if (bookIsIndexed(fileEntry.getName()))
                continue;
            else
                //Index the document
                dbConnection.postBook(fileEntry);
            //Post the vocabulary of each book
            int bookId = dbConnection.getBookId(fileEntry.getName());
            postVocabularyBook(bookId, fileEntry.getPath());
        }
    }

    /**
     * Posts the vocabulary for each book
     *
     * @param bookId  the identifier number of the book
     * @param bookUri the relative path of the book where it's stored
     */
    private void postVocabularyBook(int bookId, String bookUri) {
        HashMap<String, PostTerm> vocabulary = FileController.createBookPostList(bookId, bookUri, splitRegex);
        if (vocabulary != null)
            dbConnection.postVocabulary(vocabulary);
    }

    /**
     * Indicate if a book is previously indexed
     *
     * @param bookTitle the name of the book
     * @return a boolean that indicate if the book is indexed (true) or not (false)
     */
    private boolean bookIsIndexed(String bookTitle) {
        int IDBook = dbConnection.getBookId(bookTitle);
        return IDBook != -1;
    }

    /**
     * This is the method that search all the terms passed in the query user, and returns a list of Response
     * objects, which contains a reference to the most relevant documents indexed for that query
     *
     * @param searchQuery     sentence to search through all the documents
     * @param numberOfResults //R --> number of documents to return
     * @return A list that contains the number of documents sorted relay on the word's weight
     */
    public List<Response> search(String searchQuery, int numberOfResults) {
        ArrayList<VocabularyWord> queryUser = buildQueryUser(searchQuery);
        HashMap<Integer, Response> documentList = new HashMap<>(); //HashMap<IDDoc, Response>

        queryUser.stream().forEach(vocabularyWord -> { //iterate over query words
            //increase for each document
            double increase = Math.log10(numberOfBooks / (double) (vocabularyWord.getNr()));
            ResultSet rsTerms = dbConnection.getTerm(vocabularyWord, 50);
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
        //Sort the documents by relevanceIndex
        Collections.sort(result);
        if (result.size() < numberOfResults) return result;
        return result.subList(0, numberOfResults);
    }

    /**
     * Splits the user's query to search the terms in the vocabulary
     *
     * @param searchQuery The sentence of the user's query to search
     * @return an ArrayList with all the words and the weight of the word
     */
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

    /**
     * @param URI the sentence with a specific route and filename
     * @return true: if the file already exists in the DB
     * false: if the file isn't indexed in the DB
     */
    public boolean fileExists(String URI) {
        return dbConnection.fileExsists(URI);
    }
}