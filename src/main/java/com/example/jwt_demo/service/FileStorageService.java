package com.example.jwt_demo.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    // 🔥 CHANGE THIS PATH TO YOUR REQUIRED FOLDER
    private final String uploadDir = "";

    public FileStorageService() throws Exception {
        Files.createDirectories(Paths.get(uploadDir)); // Ensure folder exists
    }

    public String saveFile(MultipartFile file) throws Exception {
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

        Path filePath = Paths.get(uploadDir + fileName);
        Files.write(filePath, file.getBytes());

        return filePath.toString(); // return full saved path
    }
}
