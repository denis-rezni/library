package com.reznichenko.library;

import com.reznichenko.library.server.entity.Book;
import com.reznichenko.library.server.exception.BookAlreadyBorrowedException;
import com.reznichenko.library.server.exception.BookAlreadyExistsException;
import com.reznichenko.library.server.exception.NoSuchBookException;
import com.reznichenko.library.server.exception.NoSuchVisitorException;
import com.reznichenko.library.server.service.DataBase;
import com.reznichenko.library.server.service.MapDataBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DataBaseTests {

    private DataBase db; // MapDataBase has more functions than the interface it implements, but i don't test them

    private final List<Book> books = List.of(
            new Book("r2d2", "Ayn Rand", "Atlas shrugged"),
            new Book("r2d3", "Ayn Rand", "Fountainhead"),
            new Book("c3po", "eliezer yudkowsky", "hpmor"),
            new Book("w8m8", "fedor dostoevsky", "idiot")
    );

    @BeforeEach
    void beforeEach() {
        db = new MapDataBase();
    }

    @Test
    void addAndDeleteBooks() throws BookAlreadyExistsException, NoSuchBookException {
        Book book = new Book("r2d2", "Ayn Rand", "Atlas shrugged");
        db.addBook(book);
        assertThrows(BookAlreadyExistsException.class, () -> db.addBook(book));
        Book anotherBook = new Book("r2d3", "Ayn Rand", "Atlas shrugged");
        db.addBook(anotherBook);
        assertThrows(NoSuchBookException.class, () -> db.deleteBook("r2d4"));
        db.deleteBook("r2d2");
        assertThrows(NoSuchBookException.class, () -> db.deleteBook("r2d2"));
        db.addBook(book);
        db.deleteBook("r2d3");
        assertThrows(NoSuchBookException.class, () -> db.deleteBook("r2d3"));
        db.deleteBook("r2d2");
    }

    @Test
    void authorsAndNames() throws BookAlreadyExistsException, NoSuchBookException {
        for (Book book : books) {
            db.addBook(book);
        }
        assertEquals(db.getBookAuthor("r2d2"), "Ayn Rand");
        assertEquals(db.getBookName("r2d3"), "Fountainhead");
        assertEquals(db.getBookAuthor("w8m8"), "fedor dostoevsky");
        assertEquals(db.getBookName("c3po"), "hpmor");
        db.deleteBook("r2d3");
        assertThrows(NoSuchBookException.class, () -> db.getBookAuthor("r2d3"));
        assertEquals(db.getBookAuthor("r2d2"), "Ayn Rand");
        assertEquals(db.getBookName("w8m8"), "idiot");
        assertEquals(db.getBookAuthor("c3po"), "eliezer yudkowsky");
    }

    @Test
    void changeCode() throws BookAlreadyExistsException, NoSuchBookException {
        for (Book book : books) {
            db.addBook(book);
        }
        db.changeCode("r2d2", "r2d4");
        assertEquals(db.getBookAuthor("r2d4"), "Ayn Rand");
        assertEquals(db.getBookAuthor("r2d3"), "Ayn Rand");
        assertEquals(db.getBookName("r2d3"), "Fountainhead");
        assertEquals(db.getBookName("r2d4"), "Atlas shrugged");
        assertThrows(NoSuchBookException.class, () -> db.getBookAuthor("r2d2"));
        assertThrows(BookAlreadyExistsException.class, () -> db.changeCode("r2d4", "r2d3"));
        assertThrows(BookAlreadyExistsException.class, () -> db.changeCode("r2d4", "c3po"));
        assertThrows(NoSuchBookException.class, () -> db.deleteBook("r2d2"));
        db.deleteBook("r2d4");
        assertThrows(NoSuchBookException.class, () -> db.getBookAuthor("r2d4"));
        assertThrows(NoSuchBookException.class, () -> db.getBookAuthor("r2d2"));
    }

    @Test
    void lendAndReceive() throws BookAlreadyExistsException, NoSuchBookException, NoSuchVisitorException, BookAlreadyBorrowedException {
        long id1 = db.addVisitor("A", "T");
        long id2 = db.addVisitor("G", "C");
        for (Book book : books) {
            db.addBook(book);
        }
        db.lendBook(id1, books.get(0).getCode());
        db.lendBook(id1, books.get(1).getCode());
        db.lendBook(id2, books.get(2).getCode());
        db.lendBook(id2, books.get(3).getCode());
        assertEquals(db.getBorrowedBooks(id1), books.subList(0, 2));
        assertEquals(db.getBorrowedBooks(id2), books.subList(2, 4));
        assertThrows(BookAlreadyBorrowedException.class, () -> db.lendBook(id1, books.get(0).getCode()));
        assertThrows(BookAlreadyBorrowedException.class, () -> db.lendBook(id2, books.get(0).getCode()));
        db.receiveReturnedBook(books.get(0).getCode());
        assertThrows(NoSuchVisitorException.class, () -> db.lendBook(-1, books.get(0).getCode()));
        db.lendBook(id2, books.get(0).getCode());
        db.deleteBook(books.get(2).getCode());
        db.deleteBook(books.get(3).getCode());
        assertEquals(db.getBorrowedBooks(id2), books.subList(0, 1));
        db.receiveReturnedBook(books.get(0).getCode());
        assertEquals(db.getBorrowedBooks(id2), List.of());
    }



}
