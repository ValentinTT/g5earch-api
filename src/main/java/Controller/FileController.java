package Controller;

import Model.PostTerm;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;

/**
 * @author Group 5 - DLC
 * @version 2022-June
 */

public class FileController {

    /**
     * Reads the book passed by parameter (bookID and bookUri) and build the post list of the terms for that book
     *
     * @param bookID     the identifier number of the book
     * @param bookUri    the relative path of the book where it's stored
     * @param splitRegex the regular expression to filter the books
     * @return a HashMap where:
     * Key: is the term
     * Value: is an object of the class PostTerm that will be persisted
     */
    public static HashMap<String, PostTerm> createBookPostList(int bookID, String bookUri, String splitRegex) {
        HashMap<String, PostTerm> postList = new HashMap<>(100000);
        try {
            BufferedReader in = new BufferedReader(new FileReader(bookUri));
            String line;
            while ((line = in.readLine()) != null) {
                String[] words = line.split(splitRegex);
                for (String word : words) {
                    word = word.toLowerCase(Locale.ROOT);
                    PostTerm value = postList.get(word);
                    if (value == null) // First appearance in ALL indexing
                        postList.put(word, new PostTerm(bookID, word));
                    else
                        value.increase();
                }
            }
            return postList;
        } catch (FileNotFoundException e) {
            System.out.println("File not found.");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Input/Output error.");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Saves the file passed by parameter on the path of FILE_DIRECTORY
     *
     * @param file           that will be saved
     * @param FILE_DIRECTORY the path where save the file
     * @return
     * @throws IOException
     */
    public static boolean saveFile(MultipartFile file, String FILE_DIRECTORY) throws IOException {
        int indexOfType = file.getOriginalFilename().lastIndexOf(".");
        if (indexOfType >= 0) {
            //If file type is "txt" --> upload and indexOfType
            if (file.getOriginalFilename().substring(indexOfType + 1).equals("txt")) {
                File newFile = new File(FILE_DIRECTORY + file.getOriginalFilename());
                newFile.createNewFile();
                FileOutputStream fos = new FileOutputStream(newFile);
                fos.write(file.getBytes());
                fos.close();
                return true;
            }
        }
        return false;
    }

    /**
     * @param path of the file to download
     * @return the resource contained in the URL
     * @throws MalformedURLException if the URL is not formed properly
     */
    public static Resource getFile(Path path) throws MalformedURLException {
        return new UrlResource(path.toUri());
    }
}