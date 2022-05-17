package Controller;

import Model.PostTerm;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

/**
 * @author Leonel Casale weeeee
 */
public class Reader {

    public static HashMap<String, PostTerm> createBookVocabulary(int bookID, String bookUri, String splitRegex) {
        HashMap<String, PostTerm> vocabulary = new HashMap<>(100000);
        try {
            BufferedReader in = new BufferedReader(new FileReader(bookUri));
            String line;
            while ((line = in.readLine()) != null) {
                String[] words = line.split(splitRegex);
                for (String word : words) {
                    word = word.toLowerCase(Locale.ROOT);
                    PostTerm value = vocabulary.get(word);
                    if (value == null) // First appearance in ALL indexing
                        vocabulary.put(word, new PostTerm(bookID, word));
                    else
                        value.increase();
                }
            }
            //TODO: en mi opinión el return iría acá, en los catch o afuera iría null o algo así
        } catch (FileNotFoundException e) {
            System.out.println("File not found.");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Input/Output error.");
            e.printStackTrace();
        }
        return vocabulary;
    }
}
