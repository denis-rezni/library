package com.reznichenko.library.server.service;

import com.reznichenko.library.server.entity.Book;
import com.reznichenko.library.server.entity.Visitor;
import com.reznichenko.library.server.exception.BookAlreadyBorrowedException;
import com.reznichenko.library.server.exception.BookAlreadyExistsException;
import com.reznichenko.library.server.exception.NoSuchBookException;
import com.reznichenko.library.server.exception.NoSuchVisitorException;

import java.util.*;

public class MapDataBase implements DataBase {

    private final Map<String, Visitor> codeToOwner = new HashMap<>();
    private final Map<String, Book> codeToBook = new HashMap<>();
    private final Map<Long, List<Book>> borrowedBooks = new HashMap<>();
    private final Map<Long, Visitor> visitors = new HashMap<>();

    @Override
    public synchronized List<Book> getBorrowedBooks(long id) throws NoSuchVisitorException {
        checkVisitor(id);
        return borrowedBooks.get(id);
    }

    @Override
    public synchronized String getBookName(String code) throws NoSuchBookException {
        return getBookSafely(code).getName();
    }

    @Override
    public synchronized String getBookAuthor(String code) throws NoSuchBookException {
        return getBookSafely(code).getAuthor();
    }

    @Override
    public synchronized long addVisitor(String name, String surname) {
        long id = getNextId();
        Visitor newVisitor = new Visitor(id, name, surname);
        borrowedBooks.put(id, new ArrayList<>());
        visitors.put(id, newVisitor);
        return id;
    }

    @Override
    public synchronized void addBook(Book book) throws BookAlreadyExistsException {
        if (codeToBook.containsKey(book.getCode())) {
            throw new BookAlreadyExistsException("book with code: " + book.getCode() + " already exists");
        }
        codeToBook.put(book.getCode(), book);
    }

    @Override
    public synchronized void deleteBook(String code) throws NoSuchBookException {
        Book book = getBookSafely(code);
        Visitor owner = codeToOwner.get(code);
        deleteFromOwner(owner, book);
        codeToOwner.remove(code);
        codeToBook.remove(code);
    }

    @Override
    public synchronized void changeCode(String oldCode, String newCode) throws NoSuchBookException {
        Book book = getBookSafely(oldCode);
        book.setCode(newCode);
        codeToBook.put(newCode, book);
    }

    @Override
    public synchronized void lendBook(long visitorId, String code) throws NoSuchBookException, BookAlreadyBorrowedException, NoSuchVisitorException {
        Book book = getBookSafely(code);
        if (codeToOwner.containsKey(code)) {
            throw new BookAlreadyBorrowedException("book with code " + code + " is already borrowed by id " +
                    codeToOwner.get(code).getId());
        }
        Visitor visitor = getVisitorSafely(visitorId);
        codeToOwner.put(code, visitor);
        borrowedBooks.get(visitorId).add(book);
    }

    @Override
    public synchronized void receiveReturnedBook(String code) throws NoSuchBookException {
        Book book = getBookSafely(code);
        Visitor owner = codeToOwner.get(code);
        deleteFromOwner(owner, book);
        codeToOwner.remove(code);
    }


    private long getNextId() {
        return UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
    }

    private Book getBookSafely(String code) throws NoSuchBookException {
        checkBook(code);
        return codeToBook.get(code);
    }

    private Visitor getVisitorSafely(long id) throws NoSuchVisitorException {
        checkVisitor(id);
        return visitors.get(id);
    }

    private void checkVisitor(long id) throws NoSuchVisitorException {
        if (!visitors.containsKey(id) || id < 0) {
            throw new NoSuchVisitorException("no visitor found with id: " + id);
        }
    }

    private void checkBook(String code) throws NoSuchBookException {
        if (!codeToBook.containsKey(code)) {
            throw new NoSuchBookException("no book found with code: " + code);
        }
    }


    private void deleteFromOwner(Visitor owner, Book book) {
        if (owner == null) return;
        borrowedBooks.get(owner.getId()).remove(book);
    }

    //debug
    public void printBooks() {
        for (Book book : codeToBook.values()) {
            System.out.println(book);
        }
    }

    //debug
    public void printWholeLibrary() {
        System.out.println("===========");
        printBooks();
        System.out.println("-----------");
        for (Map.Entry<Long, List<Book>> entry : borrowedBooks.entrySet()) {
            Visitor v = visitors.get(entry.getKey());
            List<Book> books = entry.getValue();
            System.out.println(v + ":");
            books.forEach(System.out::println);
        }
    }

}
