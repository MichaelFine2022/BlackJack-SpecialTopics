package com.blackjack.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameLogic {
    private List<String> deck;
    private List<String> playerHand;
    private List<String> dealerHand;

    public GameLogic() {
        this.deck = new ArrayList<>();
        this.playerHand = new ArrayList<>();
        this.dealerHand = new ArrayList<>();
        initializeDeck();
    }

    private void initializeDeck() {
        String[] suits = {"♠", "♣", "♦", "♥"};
        String[] values = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};

        for (String suit : suits) {
            for (String value : values) {
                deck.add(value + suit);
            }
        }
        Collections.shuffle(deck);
    }

    public String drawCard() {
        return deck.remove(0);
    }

    public void startGame() {
        playerHand.add(drawCard());
        playerHand.add(drawCard());
        dealerHand.add(drawCard());
        dealerHand.add(drawCard());
    }

    public List<String> getPlayerHand() {
        return playerHand;
    }

    public List<String> getDealerHand() {
        return dealerHand;
    }
}
