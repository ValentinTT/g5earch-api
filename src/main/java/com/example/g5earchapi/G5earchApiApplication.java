package com.example.g5earchapi;

import Controller.Engine;
import Model.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

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
        return G5earchApiApplication.engine.search(searchQuery, 5);
//        ArrayList<Response> arr = new ArrayList<>();
//        for(int i = 0; i < Math.random()*5+3; i++) {
//            arr.add(new Response("The Hobbit", "https://www.anderson1.org/site/handlers/filedownload.ashx?moduleinstanceid=24440&dataid=44258&FileName=hobbit.pdf", "In a hole in the ground there lived a hobbit. Not a nasty, dirty, wet hole, filled with the ends of worms and an oozy smell, nor yet a dry, bare, sandy hole with nothing in it to sit down on or to eat: it was a hobbit-hole, and that means comfort. "));
//        }
//        HashMap<String, ArrayList<Response>> response = new  HashMap<>();
//        response.put("response", arr);
//        return response;
//        return null;
    }

    @PostMapping("/upload")
    public ResponseEntity<Object> fileUpload(@RequestParam("File") MultipartFile file){
        //TODO: validate type of file
        File newFile = new File(FILE_DIRECTORY+file.getOriginalFilename());
        try {
            newFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(newFile);
            fos.write(file.getBytes());
            fos.close();
            G5earchApiApplication.engine.index(false);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error.", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>("The File Uploaded Successfully", HttpStatus.OK);
    }
}
