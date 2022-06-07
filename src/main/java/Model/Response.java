package Model;

import lombok.Data;

/**
 * @author Group 5 - DLC
 * @version 2022-June
 */

@Data
public class Response implements Comparable<Response> {
    String title;
    String link;
    String preview;
    double relevanceIndex;

    /**
     *
     * @param title
     */
    public Response(String title) {
        this(title, "", "", 0);
    }

    /**
     *
     * @param title
     * @param relevanceIndex
     */
    public Response(String title, double relevanceIndex) {
        this(title, "", "", relevanceIndex);
    }

    /**
     *
     * @param title
     * @param link
     * @param preview
     * @param relevanceIndex
     */
    public Response(String title, String link, String preview, double relevanceIndex) {
        this.title = title;
        this.link = link;
        this.preview = preview;
        this.relevanceIndex = relevanceIndex;
    }

    /**
     *
     * @param increase
     */
    public void increaseScore(double increase) {
        relevanceIndex += increase;
    }

    /**
     *
     * @param o the object to compare
     * @return
     */
    @Override
    public int compareTo(Response o) {
        return Double.valueOf(o.getRelevanceIndex()).compareTo(Double.valueOf(relevanceIndex));
    }
}