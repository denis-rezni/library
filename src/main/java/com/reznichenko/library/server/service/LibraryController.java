package com.reznichenko.library.server.service;

import com.reznichenko.library.server.entity.Book;
import com.reznichenko.library.server.exception.*;
import org.junit.jupiter.api.function.Executable;
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
        return executePost(() -> db.addBook(book), "book added");
    }

    @PostMapping(params = {"id", "code"}, value = "lend-book")
    public ResponseEntity<String> lendBook(@RequestParam("id") long id,
                                           @RequestParam("code") String code) {
        return executePost(() -> db.lendBook(id, code), "book lent");
    }

    @PostMapping(params = {"code"}, value = "receive")
    public ResponseEntity<String> receiveBook(@RequestParam("code") String code) {
        return executePost(() -> db.receiveReturnedBook(code), "book returned");
    }

    @PostMapping(params = {"old", "new"}, value = "change-code")
    public ResponseEntity<String> changeCode(@RequestParam("old") String oldCode,
                                             @RequestParam("new") String newCode) {
        return executePost(() -> db.changeCode(oldCode, newCode), "code changed");
    }

    @PostMapping(params = "code", value = "delete-book")
    public ResponseEntity<String> deleteBook(@RequestParam("code") String code) {
        return executePost(() -> db.deleteBook(code), "book deleted");
    }

    @GetMapping(params = "code", value = "author")
    public ResponseEntity<String> getAuthor(@RequestParam("code") String code) {
        try {
            return positiveResponse(db.getBookAuthor(code));
        } catch (NoSuchBookException e) {
            throw new IllegalRequestException(e);
        }
    }

    @GetMapping(params = "code", value = "book-name")
    public ResponseEntity<String> getBookName(@RequestParam("code") String code) {
        try {
            return positiveResponse(db.getBookName(code));
        } catch (NoSuchBookException e) {
            throw new IllegalRequestException(e);
        }
    }

    @GetMapping(params = "id", value = "borrowed-books")
    public ResponseEntity<List<String>> getBorrowedBooks(@RequestParam("id") long id) {
        try {
            List<Book> books = db.getBorrowedBooks(id);
            return ResponseEntity.ok(books.stream().map(Object::toString).collect(Collectors.toList()));
        } catch (NoSuchVisitorException e) {
            throw new IllegalRequestException(e);
        }
    }

    private ResponseEntity<String> executePost(Executable executable, String ifSucceeds) {
        try {
            executable.execute();
        } catch (Throwable cause) {
            throw new IllegalRequestException(cause);
        }
        return positiveResponse(ifSucceeds);
    }


    private ResponseEntity<String> positiveResponse(String msg) {
        return ResponseEntity.ok(msg);
    }

    //debug
    private void print() {
        ((MapDataBase) db).printWholeLibrary();
    }

}
