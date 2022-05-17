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

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public int getNr() {
        return nr;
    }

    public void setNr(int nr) {
        this.nr = nr;
    }

    public int getTf() {
        return tf;
    }

    public void setTf(int tf) {
        this.tf = tf;
    }

    @Override
    public int compareTo(VocabularyWord o) {
        return o.getNr() - nr;
    }
}
