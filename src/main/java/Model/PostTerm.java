package Model;

import lombok.Data;

@Data
public class PostTerm {
    private int idDoc;
    private String word;
    private int count;

    public PostTerm(int idDoc, String word) {
        this.idDoc = idDoc;
        this.word = word;
        count = 1;
    }

    public void increase(){
        count += 1;
    }
}
