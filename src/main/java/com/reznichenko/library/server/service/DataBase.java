package com.reznichenko.library.server.service;

import com.reznichenko.library.server.entity.Book;
import com.reznichenko.library.server.entity.Visitor;
import com.reznichenko.library.server.exception.BookAlreadyBorrowedException;
import com.reznichenko.library.server.exception.BookAlreadyExistsException;
import com.reznichenko.library.server.exception.NoSuchBookException;
import com.reznichenko.library.server.exception.NoSuchVisitorException;

import java.util.List;

public interface DataBase {

    List<Book> getBorrowedBooks(long id) throws NoSuchVisitorException;

    String getBookName(String code) throws NoSuchBookException;

    String getBookAuthor(String code) throws NoSuchBookException;

    long addVisitor(String name, String surname);

    void addBook(Book book) throws BookAlreadyExistsException;

    void deleteBook(String code) throws NoSuchBookException;

    void changeCode(String oldCode, String newCode) throws NoSuchBookException, BookAlreadyExistsException;

    void lendBook(long visitorId, String code) throws NoSuchBookException, BookAlreadyBorrowedException, NoSuchVisitorException;

    void receiveReturnedBook(String code) throws NoSuchBookException;
}
