package com.example.g5earchapi;

import Controller.Engine;
import Model.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

@SpringBootApplication
@RestController
public class G5earchApiApplication {

    private static Engine engine;
    @Value("${file.upload-dir}")
    String FILE_DIRECTORY;

    public static void main(String[] args) {
        engine = new Engine(false);
        SpringApplication.run(G5earchApiApplication.class, args);
    }

    @GetMapping(value = "/search", produces = "application/json")
    public List<Response> search(@RequestParam(value = "text", defaultValue = "") String searchQuery) {
        return G5earchApiApplication.engine.search(searchQuery.toLowerCase(Locale.ROOT), 10);
    }

    @PostMapping("/upload")
    public ResponseEntity<Object> fileUpload(@RequestParam("File") MultipartFile file) {
        if (file == null)
            return new ResponseEntity<>("No file uploaded.", HttpStatus.BAD_REQUEST);
        Path path = Paths.get(FILE_DIRECTORY + file.getOriginalFilename());
        if (G5earchApiApplication.engine.fileExists(path.toString())) {
            return new ResponseEntity<>("File " + file.getOriginalFilename() + " already exists.", HttpStatus.NOT_FOUND);
        }

        File newFile = new File(FILE_DIRECTORY + file.getOriginalFilename());
        try {
            int indexOfType = newFile.getName().lastIndexOf(".");
            if (indexOfType >= 0) {
                //If file type is "txt" --> upload and indexOfType
                if (file.getOriginalFilename().substring(indexOfType + 1).equals("txt")) {
                    //TODO: extract this code in Reader?
                    newFile.createNewFile();
                    FileOutputStream fos = new FileOutputStream(newFile);
                    fos.write(file.getBytes());
                    fos.close();
                    G5earchApiApplication.engine.index(false);
                    return new ResponseEntity<>("The File Was Successfully Uploaded", HttpStatus.OK);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>("Wrong file type.", HttpStatus.BAD_REQUEST);
    }

    private static void printFile(File newFile) {
        try {
            Scanner sc = new Scanner(newFile);
            while (sc.hasNext()) {
                System.out.println(sc.next());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<Resource> fileDownload(@PathVariable String fileName) {
        Path path = Paths.get(FILE_DIRECTORY + fileName);
        if (!G5earchApiApplication.engine.fileExists(path.toString())) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Resource resource;
        try {
            resource = new UrlResource(path.toUri());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}