 package com.example.jwt_demo.util;


import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;


/*  @author Sunil Mishra */
 
 

 @Component
public class CreateJsonFile {
	
	    private static final Logger log = LogManager.getLogger(CreateJsonFile.class);
	    
	    @Value("${file.path.english}")
	    private String englishFilePath;

	    @Value("${file.path.kannada}")
	    private String kannadaFilePath;
	    
	   

	    @SuppressWarnings("unchecked")
	    public void writeFile(List<Map<String, Object>> rows, int fileType) {
	        if (rows.isEmpty()) {
	            return; // No need to continue if the list is empty
	        }
	        JSONObject jsonObject = new JSONObject();
	        for (Map<String, Object> row : rows) {
	            jsonObject.put(row.get("key"), row.get("name"));
	        }
	        try {
	            String fileName;
	            switch (fileType) {
	                case 1:
	                    fileName = englishFilePath;
	                    break;
	                case 2:
	                    fileName = kannadaFilePath;
	                    break;
	               
	                default:
	                    fileName = "InvalidFileType.json";
	            }

	            if (StringUtils.hasText(fileName)) {
	                try (Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8"))) {
	                    out.write(jsonObject.toJSONString());
	                } catch (IOException e) {
	                    e.printStackTrace();
	                }
	            } else {
	                System.err.println("Invalid file path");
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
	    
}
 