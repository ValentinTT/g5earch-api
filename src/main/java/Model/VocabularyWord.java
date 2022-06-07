package Model;

import lombok.Data;

/**
 * @author Group 5 - DLC
 * @version 2022-June
 */

@Data
public class VocabularyWord implements Comparable<VocabularyWord> {
    private String word;
    private int nr;
    private int tf;

    /**
     *
     * @param word
     * @param nr
     * @param tf
     */
    public VocabularyWord(String word, int nr, int tf) {
        this.word = word;
        this.nr = nr;
        this.tf = tf;
    }

    /**
     *
     * @param o the object to compare
     * @return
     */
    @Override
    public int compareTo(VocabularyWord o) {
        return o.getNr() - nr;
    }
}
