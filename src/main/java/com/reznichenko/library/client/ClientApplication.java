package com.reznichenko.library.client;

import okhttp3.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ClientApplication {

    private final String BASE_URL = "http://localhost:8080/";
    private final String END_STRING = "end";
    private final String HELP_STRING = "help";

    private final String HELP_MESSAGE = "add-book name author code -- adds a new book with a given name, author and unique code" + System.lineSeparator() +
            "book-name code -- prints the name of the book with the given code" + System.lineSeparator() +
            "author code -- prints the author of a book with the given code" + System.lineSeparator() +
            "add-visitor name surname -- adds a new visitor. prints the id of this visitor" + System.lineSeparator() +
            "lend id code -- lends a book to a given visitor" + System.lineSeparator() +
            "receive code -- returns the book back to the library" + System.lineSeparator() +
            "change-code -- changes the code of a given book" + System.lineSeparator() +
            "delete-book code -- deletes a book from the library" + System.lineSeparator() +
            "list id -- lists all the books borrowed by visitor with the given id" + System.lineSeparator() +
            "help -- shows this message" + System.lineSeparator() +
            "end -- terminates the application";

    OkHttpClient client = new OkHttpClient();
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    private final Map<String, Command> commands = Map.ofEntries(
            Map.entry("add-book",
                    new Command("add-book", List.of("name", "author", "code"), RequestType.POST)),
            Map.entry("book-name",
                    new Command("book-name", List.of("code"), RequestType.GET)),
            Map.entry("author",
                    new Command("author", List.of("code"), RequestType.GET)),
            Map.entry("add-visitor",
                    new Command("add-visitor", List.of("name", "surname"), RequestType.POST)),
            Map.entry("lend",
                    new Command("lend-book", List.of("id", "code"), RequestType.POST)),
            Map.entry("receive",
                    new Command("receive", List.of("code"), RequestType.POST)),
            Map.entry("change-code",
                    new Command("change-code", List.of("old", "new"), RequestType.POST)),
            Map.entry("delete-book",
                    new Command("delete-book", List.of("code"), RequestType.POST)),
            Map.entry("list",
                    new Command("borrowed-books", List.of("id"), RequestType.GET))
    );


    private void run() throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            String[] split = line.split("\\s+");
            if (split.length < 1) {
                continue;
            }
            String type = split[0];
            if (type.equals(END_STRING)) {
                return;
            }
            if (type.equals(HELP_STRING)) {
                System.out.println(HELP_MESSAGE);
                continue;
            }
            try {
                if (commands.containsKey(type)) {
                    Command command = commands.get(type);
                    processCommand(command, split);
                } else {
                    throw new UnknownCommandException("no such command:" + type + ", use help");
                }
            } catch (UnknownCommandException e) {
                //no need to stop the execution
                System.out.println(e.getMessage());
            }
        }

    }

    private void processCommand(Command command, String[] line) throws UnknownCommandException, IOException {
        List<String> args = Arrays.stream(line).skip(1).collect(Collectors.toList());
        if (args.size() != command.argNames.size()) {
            throw new UnknownCommandException("command " + line[0] + " has "
                    + command.argNames.size() + " arguments");
        }
        Response response;
        switch (command.type) {
            case POST: {
                response = getPostResponse(command, args);
                break;
            }
            case GET: {
                response = getGetResponse(command, args);
                break;
            }
            default: {
                throw new UnknownCommandException("no command know for request type: " + command.type);
            }
        }
        if (response.body() == null) {
            System.out.println();
        } else {
            System.out.println(Objects.requireNonNull(response.body()).string());
        }

    }

    private Response getPostResponse(Command command, List<String> args) throws IOException {
        FormBody.Builder bodyBuilder = new FormBody.Builder();
        for (int i = 0; i < args.size(); i++) {
            bodyBuilder.add(command.argNames.get(i), args.get(i));
        }
        RequestBody body = bodyBuilder.build();
        Request req = new Request.Builder()
                .url(BASE_URL + command.request)
                .post(body)
                .build();
        return client.newCall(req).execute();
    }

    private Response getGetResponse(Command command, List<String> args) throws IOException {
        HttpUrl.Builder builder = Objects.requireNonNull(HttpUrl.parse(BASE_URL + command.request)).newBuilder();
        for (int i = 0; i < args.size(); i++) {
            builder.addQueryParameter(command.argNames.get(i), args.get(i));
        }
        String url = builder.build().toString();
        Request req = new Request.Builder()
                .url(url)
                .build();
        return client.newCall(req).execute();
    }

    private static class Command {
        List<String> argNames;
        String request;
        RequestType type;

        private Command(String request, List<String> argNames, RequestType type) {
            this.argNames = argNames;
            this.request = request;
            this.type = type;
        }
    }

    private enum RequestType {
        POST, GET
    }

    private static class UnknownCommandException extends Exception {
        public UnknownCommandException(String message) {
            super(message);
        }
    }


    public static void main(String[] args) {
        try {
            new ClientApplication().run();
        } catch (IOException e) {
            System.out.println("IO exception occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
