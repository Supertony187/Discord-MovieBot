package de.supertony;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

/*
 * Movie Bot
 *
 * Copyright (c) 2023 Supertony
 */

public class Main {

    private JDA jda;
    private File movieListFile;
    private List<Movie> movieList;

    public Main(String token) throws IOException {
        this.jda = JDABuilder.createDefault(token).enableIntents(GatewayIntent.MESSAGE_CONTENT).build();
        this.movieListFile = new File("movie_list.json");
        this.movieList = new ArrayList<>();

        if (!movieListFile.exists()) {
            movieListFile.createNewFile();
        }

        loadMovieList();

        jda.addEventListener(new MyEventListener());
    }

    private class MyEventListener extends ListenerAdapter {
        @Override
        public void onMessageReceived(MessageReceivedEvent event) {
            Message message = event.getMessage();
            if (message.getContentRaw().startsWith("!movie")) {
                String[] commandArgs = message.getContentRaw().split(" ");
                if (commandArgs.length >= 2) {
                    String command = commandArgs[1];

                    switch (command) {
                        case "list":
                            listMovies(message.getChannel());
                            break;
                        case "add":
                            if (commandArgs.length >= 3) {
                                addMovie(message.getChannel(), commandArgs[2], message.getAuthor());
                            }
                            break;
                        case "countdown":
                            if (commandArgs.length >= 3) {
                                countDown(message.getChannel(), commandArgs[2]);
                            }
                            break;
                        default:
                            message.getChannel().sendMessage("Unbekannter Befehl").queue();
                    }
                }
            }
        }
    }

    private void listMovies(MessageChannel channel) {
        channel.sendMessage("```\n" +
                "Filme in der Liste:\n" +
                "Name | Autor | Countdown\n" +
                "-----|--------|--------\n"
        ).queue();
        for (Movie movie : movieList) {
            channel.sendMessage(movie.toString()).queue();
        }
        channel.sendMessage("```").queue();
    }

    private void addMovie(MessageChannel channel, String movieName, User author) {
        if (movieExists(movieName)) {
            channel.sendMessage("Der Film " + movieName + " ist bereits in der Liste.").queue();
            return;
        }

        Movie movie = new Movie(movieName, author, new Date(), 0);
        movieList.add(movie);
        saveMovieList();
        channel.sendMessage("Der Film " + movieName + " wurde erfolgreich zur Liste hinzugefügt.").queue();
    }

    private void countDown(MessageChannel channel, String movieName) {
        Movie movie = getMovie(movieName);
        if (movie == null) {
            channel.sendMessage("Der Film " + movieName + " ist nicht in der Liste.").queue();
            return;
        }

        Date now = new Date();
        Date releaseDate = movie.getReleaseDate();
        long diff = releaseDate.getTime() - now.getTime();
        long days = diff / (24 * 60 * 60 * 1000);

        if (days <= 0) {
            channel.sendMessage("Der Film " + movieName + " wird heute um 0:00 Uhr veröffentlicht.").queue();
        } else {
            channel.sendMessage("Der Film " + movieName + " wird in " + days + " Tagen veröffentlicht.").queue();
        }
    }

    private boolean movieExists(String movieName) {
        for (Movie movie : movieList) {
            if (movie.getName().equalsIgnoreCase(movieName)) {
                return true;
            }
        }
        return false;
    }

    private void loadMovieList() {
    }

    private void saveMovieList() {
    }

    private Movie getMovie(String movieName) {
        for (Movie movie : movieList) {
            if (movie.getName().equalsIgnoreCase(movieName)) {
                return movie;
            }
        }
        return null;
    }

    public static void main(String[] args) {
        try {
            new Main("YOUR TOKEN DU RENTNER");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class Movie {
    private String name;
    private User author;
    private Date releaseDate;
    private int countdown;

    public Movie(String name, User author, Date releaseDate, int countdown) {
        this.name = name;
        this.author = author;
        this.releaseDate = releaseDate;
        this.countdown = countdown;
    }

    public String getName() {
        return name;
    }

    public User getAuthor() {
        return author;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public int getCountdown() {
        return countdown;
    }

    @Override
    public String toString() {
        return String.format("%s | %s | %d", name, author.getName(), countdown);
    }
}
