package Model;

import lombok.Data;

@Data
public class VocabularyWord implements Comparable<VocabularyWord> {
    private String word;
    private int nr;
    private int tf;

    public VocabularyWord(String word, int nr, int tf) {
        this.word = word;
        this.nr = nr;
        this.tf = tf;
    }


    @Override
    public int compareTo(VocabularyWord o) {
        return nr - o.getNr();
    }
}
