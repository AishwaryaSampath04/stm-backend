package com.example.jwt_demo.model;

import javax.persistence.*;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

@Entity

@Table(name = "todos")
@SQLDelete(sql = "UPDATE todos SET delete = true WHERE id = ?")
@Where(clause = "delete = false")
public class Todo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 250)
    private String title;

    @Column(length = 250)
    private String description;

    @Column(length = 250)
    private int quantity;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(length = 250)
    private String tag;

    @Column(length = 250)
    private String status;

    @Column(nullable = false)
    private Boolean active = true; // ✅ new field

      @Column(name = "delete") // match your DB column name
    private Boolean delete = false;


    // Constructors
    public Todo() {
    }

    public Todo(String title, String description, int quantity, LocalDateTime dueDate, String tag, String status, Boolean active ) {
        this.title = title;
        this.description = description;
        this.quantity = quantity;
        this.dueDate = dueDate;
        this.tag = tag;
        this.status = status;
       this.active = active != null ? active : true; // default true if null
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
























        this.description = description;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getActive() {
        return active;
    } // ✅ getter

    public void setActive(Boolean active) {
        this.active = active;
    } // ✅ setter


     public Boolean getDelete() {
        return delete;
    }

    public void setDelete(Boolean delete) {
        this.delete = delete;
    }

    
    @Override
    public String toString() {
        return "Todo{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", quantity='" + quantity + '\'' +
                ", dueDate=" + dueDate +
                ", tag='" + tag + '\'' +
                ", status='" + status + '\'' +
                ", active=" + active + // ✅ include in toString
                '}';
    }
}
