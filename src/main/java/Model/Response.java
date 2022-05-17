package Model;

import lombok.Data;

import java.util.function.DoubleBinaryOperator;

@Data
public class Response implements Comparable<Response>{
    String title;
    String link;
    String preview;
    double relevanceIndex;

    public Response(String title) {
        this(title, "", "", 0);
    }
    public Response(String title, double relevanceIndex) { this(title, "", "", relevanceIndex); }

    public Response(String title, String link, String preview, double relevanceIndex) {
        this.title = title;
        this.link = link;
        this.preview = preview;
        this.relevanceIndex = relevanceIndex;
    }

    public void increaseScore(double increase) { relevanceIndex += increase; }

    @Override
    public int compareTo(Response o) {
        return Double.valueOf(relevanceIndex).compareTo(Double.valueOf(o.getRelevanceIndex()));
    }
}