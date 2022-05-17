package Model;

import lombok.Data;

@Data
public class Response{
    String title;
    String link;
    String preview;
    int score;

    public Response(String title) {
        this(title, "", "", 0);
    }
    public Response(String title, int score) { this(title, "", "", score); }

    public Response(String title, String link, String preview, int score) {
        this.title = title;
        this.link = link;
        this.preview = preview;
        this.score = score;
    }

    public void increaseScore(int increase) { score += increase; }
}