package com.reznichenko.library.server.service;

import com.reznichenko.library.server.entity.Book;
import com.reznichenko.library.server.exception.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class LibraryController {

    private final DataBase db = new MapDataBase();

    @PostMapping(params = {"name", "surname"}, value = "add-visitor")
    public ResponseEntity<String> addVisitor(@RequestParam("name") String name,
                                             @RequestParam("surname") String surname) {
        long id = db.addVisitor(name, surname);
        return positiveResponse(String.valueOf(id));
    }

    @PostMapping(params = {"name", "author", "code"}, value = "add-book")
    public ResponseEntity<String> addBook(@RequestParam("name") String name,
                                          @RequestParam("author") String author,
                                          @RequestParam("code") String code) {
        Book book = new Book(code, author, name);
        try {
            db.addBook(book);
        } catch (BookAlreadyExistsException e) {
            throw new IllegalRequestException("bad request: " + e.getMessage(), e);
        }
        return positiveResponse("book added");
    }

    @PostMapping(params = {"id", "code"}, value = "lend-book")
    public ResponseEntity<String> lendBook(@RequestParam("id") long id,
                                           @RequestParam("code") String code) {
        try {
            db.lendBook(id, code);
        } catch (NoSuchVisitorException | NoSuchBookException | BookAlreadyBorrowedException e) {
            throw new IllegalRequestException("bad request: " + e.getMessage(), e);
        }
        return positiveResponse("book lent");
    }

    @PostMapping(params = {"code"}, value = "receive")
    public ResponseEntity<String> receiveBook(@RequestParam("code") String code) {
        try {
            db.receiveReturnedBook(code);
        } catch (NoSuchBookException e) {
            throw new IllegalRequestException("bad request: " + e.getMessage(), e);
        }
        return positiveResponse("book returned");
    }

    @PostMapping(params = {"old", "new"}, value = "change-code")
    public ResponseEntity<String> changeCode(@RequestParam("old") String oldCode,
                                             @RequestParam("new") String newCode) {
        try {
            db.changeCode(oldCode, newCode);
        } catch (NoSuchBookException e) {
            throw new IllegalRequestException("bad request: " + e.getMessage(), e);
        }
        return positiveResponse("code changed");
    }

    @PostMapping(params = "code", value = "delete-book")
    public ResponseEntity<String> deleteBook(@RequestParam("code") String code) {
        try {
            db.deleteBook(code);
        } catch (NoSuchBookException e) {
            throw new IllegalRequestException("bad request: " + e.getMessage(), e);
        }
        return positiveResponse("book deleted");
    }

    @GetMapping(params = "code", value = "author")
    public ResponseEntity<String> getAuthor(@RequestParam("code") String code) {
        try {
            return positiveResponse(db.getBookAuthor(code));
        } catch (NoSuchBookException e) {
            throw new IllegalRequestException("bad request: " + e.getMessage(), e);
        }
    }

    @GetMapping(params = "code", value = "book-name")
    public ResponseEntity<String> getBookName(@RequestParam("code") String code) {
        try {
            return positiveResponse(db.getBookName(code));
        } catch (NoSuchBookException e) {
            throw new IllegalRequestException("bad request: " + e.getMessage(), e);
        }
    }

    @GetMapping(params = "id", value = "borrowed-books")
    public ResponseEntity<List<String>> getBorrowedBooks(@RequestParam("id") long id) {
        try {
            List<Book> books = db.getBorrowedBooks(id);
            return ResponseEntity.ok(books.stream().map(Object::toString).collect(Collectors.toList()));
        } catch (NoSuchVisitorException e) {
            throw new IllegalRequestException("bad request: " + e.getMessage(), e);
        }
    }

    private ResponseEntity<String> positiveResponse(String msg) {
        return ResponseEntity.ok(msg);
    }


    private void print() {
        ((MapDataBase) db).printWholeLibrary();
    }

}
