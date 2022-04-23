package Controller;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class Engine {
    HashMap<String, ArrayList<Integer>> vocabulary;
    DBConnection connection;
    public Engine(){
        vocabulary = new HashMap<>();
        connection = DBConnection.createConnection();
    }

    public void index(boolean force) {
        //if(force) //remove db)
        final File folder = new File("src/main/resources/static/documentos");
        for (final File fileEntry : folder.listFiles()) {
            System.out.println(fileEntry.getName());
        }
    }

    public void addBook() {

    }
}
