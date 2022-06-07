package com.example.g5earchapi;

import Controller.Engine;
import Controller.FileController;
import Model.Response;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;

/**
 * @author Group 5 - DLC
 * @version 2022-June
 */

@SpringBootApplication
@RestController
public class G5earchApiApplication {
    private static Engine engine;
    private static final String FILE_DIRECTORY = "src/main/resources/static/";

    /**
     * @param args
     */
    public static void main(String[] args) {
        engine = new Engine(false, FILE_DIRECTORY);
        SpringApplication.run(G5earchApiApplication.class, args);
    }

    /**
     * Endpoint to search a sentence through all the documents
     *
     * @param searchQuery sentece to look for
     * @return a list with all the documents that contains any or all the words in the query as Response Object
     */
    @GetMapping(value = "/search", produces = "application/json")
    public List<Response> search(@RequestParam(value = "text", defaultValue = "") String searchQuery) {
        return G5earchApiApplication.engine.search(searchQuery.toLowerCase(Locale.ROOT), 10);
    }

    /**
     * Endpoint to upload a file
     *
     * @param file file to upload
     * @return if uploaded: returns 200 OK
     * if no file was provided or the type isn't .txt: returns Bad Request (400)
     * if the file already exists: returns Conflict (409)
     * if an exception rise: returns Internal Sever Error (500)
     */
    @PostMapping("/upload")
    public ResponseEntity<Object> fileUpload(@RequestParam("File") MultipartFile file) {
        if (file == null)
            return new ResponseEntity<>("No file uploaded.", HttpStatus.BAD_REQUEST);
        Path path = Paths.get(FILE_DIRECTORY + file.getOriginalFilename());
        if (engine.fileExists(path.toString())) {
            return new ResponseEntity<>("File " + file.getOriginalFilename() + " already exists.", HttpStatus.CONFLICT);
        }
        try {
            if (FileController.saveFile(file, FILE_DIRECTORY)) {
                engine.index(false);
                return new ResponseEntity<>("The File Uploaded Successfully", HttpStatus.OK);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>("Wrong file type.", HttpStatus.BAD_REQUEST);
    }

    /**
     * Endpoint to download and specific document if exists
     *
     * @param fileName name of the document that want's to be downloaded
     * @return if exists: an object ResponseEntity that contains the file
     * if doesn't exists: rise a 404 error
     */
    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<Resource> fileDownload(@PathVariable String fileName) {
        Path path = Paths.get(FILE_DIRECTORY + fileName);
        if (!engine.fileExists(path.toString())) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Resource resource;
        try {
            resource = FileController.getFile(path);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}