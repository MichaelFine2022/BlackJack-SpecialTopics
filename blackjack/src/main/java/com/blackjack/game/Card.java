package com.blackjack.game;

public class Card {
    private String suit;
    private String rank;
    private int value;

    public Card(String suit, String rank) {
        this.suit = suit;
        this.rank = rank;
        if(rank.equals("A")){
            value = (Integer) 0;
        }
        else if(rank.equals("K")){
            value = 10;
        }
        else if(rank.equals("Q")){
            value = 10;
        }
        else if(rank.equals("J")){
            value = 10;
        }
        else{
            value = Integer.parseInt(rank);
        }
    }

    public String getSuit() { return suit; }
    public String getRank() { return rank; }
    public int getValue() { return value; }

    @Override
    public String toString() {
        return rank + " of " + suit;
    }
}
