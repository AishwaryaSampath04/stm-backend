package com.example.jwt_demo.controller;

import com.example.jwt_demo.model.Todo;
import com.example.jwt_demo.repository.TodoRepository;
import com.example.jwt_demo.util.CreateJsonFile;
import com.example.jwt_demo.util.CreateJsonFile;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;





@CrossOrigin(origins = "*")

@RestController
@RequestMapping("/api/todos")
public class TodoController {

    @Autowired
    private TodoRepository todoRepository;




    @Autowired
    JdbcTemplate jdbcTemplate;

    Gson gson = new GsonBuilder().enableComplexMapKeySerialization().serializeNulls().setPrettyPrinting()
            .setVersion(1.0).create();

    // ✅ Get all todos
    @GetMapping
    public Page<Todo> getAllTodos(Pageable pageable) {
        //return todoRepository.findAll();
        return todoRepository.findAllActive(pageable);

    }



@GetMapping(value = "/getLangType")
	public @ResponseBody String getLangType() {
		String sql ="select * from language_m where active=true and to_be_display=true order by display_order";
		 try {
			List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
			return gson.toJson(rows);
		 } catch (RuntimeException e) {
			throw e;
		 } catch (Exception e) {
			throw new RuntimeException(e);
		 }
	}

@GetMapping(value ="/languages")
public List<Map<String, Object>> getAllLanguages() {
    String sql = "SELECT * FROM language_m";
    return jdbcTemplate.queryForList(sql);
}



@Autowired
private CreateJsonFile createJsonFile;

 @GetMapping(value = "/writeJsonFile",produces = "application/json;charset=UTF-8")
	public @ResponseBody String writeJsonFile() { 
	    int fileType1 = 1;   // for english
	    int fileType2 = 2;   // for Kannada
	  
	    try {
	    	 // Getting English Label
	    	 String query_en = "select name,label_key as key from label_m Where lang_id = 1";
	    	 @SuppressWarnings("unchecked")
			 List<Map<String, Object>> rows_en = jdbcTemplate.queryForList(query_en);
	    	 createJsonFile.writeFile(rows_en, fileType1);
	    	 
	    	 // Getting Kannada Label
	    	 String query_kn = "select name,label_key as key from label_m Where lang_id = 2";
	    	 @SuppressWarnings("unchecked")
			 List<Map<String, Object>> rows_kn = jdbcTemplate.queryForList(query_kn);
	    	 createJsonFile.writeFile(rows_kn, fileType2);
	    
			 return gson.toJson(rows_en);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
    // ✅ Get a todo by ID
    @GetMapping("/{id}")
    public ResponseEntity<Todo> getTodoById(@PathVariable Long id) {
        Optional<Todo> todo = todoRepository.findById(id);
        return todo.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ✅ Create a new todo
    @PostMapping
    public Todo createTodo(@RequestBody Todo todo) {
        return todoRepository.save(todo);
    }

    // ✅ Update an yexisting todo
    @PutMapping("/{id}")
    public ResponseEntity<Todo> updateTodo(@PathVariable Long id, @RequestBody Todo todoDetails) {
        return todoRepository.findById(id)
                .map(todo -> {
                    todo.setTitle(todoDetails.getTitle());
                    todo.setDescription(todoDetails.getDescription());
                    todo.setQuantity(todoDetails.getQuantity());
                    todo.setDueDate(todoDetails.getDueDate());
                    todo.setTag(todoDetails.getTag());
                    todo.setStatus(todoDetails.getStatus());
                    todo.setActive(todoDetails.getActive()); // ✅ update active
                    Todo updated = todoRepository.save(todo);
                    return ResponseEntity.ok(updated);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /* // ✅ Delete a todo
    @DeleteMapping("/{id}")
       public ResponseEntity<Void> deleteTodo(@PathVariable Long id) {
        if (todoRepository.existsById(id)) {
           // todoRepository.deleteById(id);
           Optional<Todo> optionalTodo = todoRepository.findById(id);
           if (optionalTodo.isPresent()){
             Todo todo = optionalTodo.get();
              todo.setDelete(true); 
              todoRepository.save(todo); 
           }
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
 */
    

 @DeleteMapping("/{id}")
public ResponseEntity<Void> deleteTodo(@PathVariable Long id) {
    Optional<Todo> optionalTodo = todoRepository.findById(id);
    if (optionalTodo.isPresent()) {
        Todo todo = optionalTodo.get();
        todo.setDelete(true); // only set delete = true
        todoRepository.save(todo); // save without touching other data
        return ResponseEntity.noContent().build();
    }
    return ResponseEntity.notFound().build();
}

// ✅ Only mark active = false, do not modify anything else
 @PostMapping("/setStatus/{id}")
public ResponseEntity<Void> updateActiveStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> updateData) {
    Optional<Todo> optionalTodo = todoRepository.findById(id);
    if (optionalTodo.isPresent()) {
        Todo todo = optionalTodo.get();
        if (updateData.containsKey("active")) {
            todo.setActive(updateData.get("active"));
            todoRepository.save(todo);
        }
        return ResponseEntity.noContent().build();
    }
    return ResponseEntity.notFound().build();
}



    @GetMapping(value = "/search")
    public List<Todo> searchItem(@RequestParam(value = "key", required = true) String key) {
        try {

            return todoRepository
                    .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrTagContainingIgnoreCaseOrStatusContainingIgnoreCase(
                            key, key, key, key);
        } catch (DataAccessException e) {
            // Handle database access exceptions
            throw new RuntimeException("Error accessing database", e);
        } catch (RuntimeException e) {
            // Handle other runtime exceptions
            throw e;
        }
    }
@GetMapping("/todos/search/status")
public List<Todo> searchByStatus(@RequestParam String status) {
    return todoRepository.findByStatus(status);
}


    @GetMapping("/page")
    public Page<Todo> getTodosByPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String key) {

        Pageable pageable = PageRequest.of(page, size);

        boolean hasStatus = status != null && !status.trim().isEmpty();
        boolean hasKey = key != null && !key.trim().isEmpty();

        if (hasStatus && hasKey) {
            // Search by both keyword and status
            return todoRepository.findByStatusAndKeyword(status, key, pageable);
        } else if (hasStatus) {
            // Search only by status
            return todoRepository.findByStatusIgnoreCase(status, pageable);
        } else if (hasKey) {
            // Search only by keyword
            return todoRepository
                    .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrTagContainingIgnoreCaseOrStatusContainingIgnoreCase(
                            key, key, key, key, pageable);
        } else {
            // No search, return all
            //return todoRepository.findAll(pageable);
             return todoRepository.findAll(pageable);
        }
    }

    @GetMapping(value = "/getStatuse")
    public String getPlans() {

        String sql = "select distinct status from todos order by 1";
        List<Map<String, Object>> arrayList = jdbcTemplate.queryForList(sql);
        return gson.toJson(arrayList);
    }
    
    
     @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public ResponseEntity<Map<String, Object>> uploadFile(
        @RequestParam("file") MultipartFile file,
        HttpServletRequest request) {

    System.out.println("Authorization header: " + request.getHeader("Authorization"));

    Map<String, Object> responseBody = new HashMap<>(); // <-- declare this first
    String uploadDir = "E:\\lekhana\\files"; // folder to save files
    String fileName = file.getOriginalFilename();

    if (file != null && !file.isEmpty()) {
        long maxFileSize = 2 * 1024 * 1024; // 2 MB in bytes
        if (file.getSize() > maxFileSize) {
            responseBody.put("message", "File size exceeds the 5 MB limit!");
            return ResponseEntity.ok(responseBody); // returns JSON
        }
    } else {
        responseBody.put("message", "No file selected!");
        return ResponseEntity.ok(responseBody);
    }

    try {
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();

        Path filePath = Paths.get(uploadDir, fileName);
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        }

        responseBody.put("message", "Uploaded: " + fileName);
        responseBody.put("size", file.getSize());

        return ResponseEntity.ok(responseBody);

    } catch (Exception e) {
        e.printStackTrace();
        responseBody.put("message", "Upload failed: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body(responseBody);
    }
}



}
