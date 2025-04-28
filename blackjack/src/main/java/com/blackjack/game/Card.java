package com.blackjack.game;

import javafx.scene.image.Image;
import java.io.InputStream;

public class Card {
    @SuppressWarnings("FieldMayBeFinal")
    private String suit;
    final private String rank;
    @SuppressWarnings("FieldMayBeFinal")
    private int value;
    public Image sprite;

    @SuppressWarnings("ConvertToStringSwitch")
    public Card(String suit, String rank) {
        this.suit = suit;
        this.rank = rank;
        if (rank.equals("A")) {
            value = 11; // Default Ace value to 11
        } else if (rank.equals("K") || rank.equals("Q") || rank.equals("J")) {
            value = 10;
        } else {
            value = Integer.parseInt(rank);
        }
        this.sprite = loadImage();
    }

    private Image loadImage() {
        String imageName = rank.toUpperCase() + suit.toUpperCase() + ".png";
        InputStream inputStream = getClass().getResourceAsStream("/cards/" + imageName);
        if (inputStream == null) {
            System.err.println("Could not load image: /cards/" + imageName);
            return null;
        }
        return new Image(inputStream);
    }

    public String getSuit() {
        return suit;
    }

    public String getRank() {
        return rank;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return rank + " of " + suit;
    }
}