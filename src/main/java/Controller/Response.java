package Controller;

import lombok.Data;

@Data
public class Response{
    String title;
    String link;
    String preview;
    //double score;

    public Response(String title, String link, String preview) {
        this.title = title;
        this.link = link;
        this.preview = preview;
    }
}