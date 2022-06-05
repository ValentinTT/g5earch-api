package Model;

import lombok.Data;

/**
 * @author Group 5 - DLC
 * @version 2022-June
 */

@Data
public class PostTerm {
    private int idDoc;
    private String word;
    private int count;

    /**
     *
     * @param idDoc
     * @param word
     */
    public PostTerm(int idDoc, String word) {
        this.idDoc = idDoc;
        this.word = word;
        count = 1;
    }

    /**
     * increase the amount of times that a terms appears in a document
     */
    public void increase(){
        count += 1;
    }
}