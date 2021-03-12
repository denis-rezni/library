package com.reznichenko.library.server.entity;

import java.util.Objects;

public class Book {
    private final String author;
    private final String name;
    private String code;

    public Book(String code, String author, String name) {
        this.code = code;
        this.author = author;
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return Objects.equals(code, book.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return author + " - " + name + " (" + code + ")";
    }
}
