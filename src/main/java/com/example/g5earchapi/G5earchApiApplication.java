package com.example.g5earchapi;

import Controller.Engine;
import Controller.Response;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;

@SpringBootApplication
@RestController
public class G5earchApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(G5earchApiApplication.class, args);
    }

    @GetMapping(value = "/buscar", produces = "application/json")
    public HashMap<String, ArrayList<Response>> sayHello(@RequestParam(value = "text", defaultValue = "") String name) {
        ArrayList<Response> arr = new ArrayList<>();
        for(int i = 0; i < Math.random()*5+3; i++) {
            arr.add(new Response("The Hobbit", "https://www.anderson1.org/site/handlers/filedownload.ashx?moduleinstanceid=24440&dataid=44258&FileName=hobbit.pdf", "In a hole in the ground there lived a hobbit. Not a nasty, dirty, wet hole, filled with the ends of worms and an oozy smell, nor yet a dry, bare, sandy hole with nothing in it to sit down on or to eat: it was a hobbit-hole, and that means comfort. "));
        }
        HashMap<String, ArrayList<Response>> response = new  HashMap<>();
        response.put("response", arr);
        return response;
    }
    // /subir?libro=010101010101001011010010101 motor.add(libro.txt, titulo)
}
