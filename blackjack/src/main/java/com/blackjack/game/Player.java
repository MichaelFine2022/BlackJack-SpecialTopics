package com.blackjack.game;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private String name;
    private List<Card> hand;

    public Player(String name) {
        this.name = name;
        this.hand = new ArrayList<>();
    }

    public void receiveCard(Card card) {
        hand.add(card);
    }

    public int getScore() {
        int score = 0;
        int aceCount = 0;
        for (Card card : hand) {
            int value = card.getValue();
            if (value > 10) {
                score += 10;
            } else {
                score += value;
            }
            if (card.getRank().equals("A")) {
                aceCount++;
            }
        }
        //treats an ace as 1 instead of 11 when advantageous for the player
        while (score > 21 && aceCount > 0) {
            score -= 10;
            aceCount--;
        }
        return score;
    }

    public String getName() {
        return name;
    }

    public List<Card> getHand() {
        return hand;
    }

    // optional method to clear the hand for a new round
    public void clearHand() {
        hand.clear();
    }

    public void setHand(ArrayList arrayList) {
        hand = arrayList;
    }
}