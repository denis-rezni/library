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

    OkHttpClient client = new OkHttpClient();
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    private final Map<String, Command> commands = Map.ofEntries(
            Map.entry("add-book",
                    new Command("add-book", List.of("name", "author", "code"), RequestType.POST)),
            Map.entry("book-name",
                    new Command("book-name", List.of("code"), RequestType.GET)),
            Map.entry("author",
                    new Command("author", List.of("code"), RequestType.GET))
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
        //TODO
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
