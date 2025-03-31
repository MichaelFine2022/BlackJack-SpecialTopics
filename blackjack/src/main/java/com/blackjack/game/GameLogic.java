package com.blackjack.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameLogic {
    private List<Card> deck;
    private List<Card> playerHand;
    private List<Card> dealerHand;

    public GameLogic() {
        this.deck = new ArrayList<>();
        this.playerHand = new ArrayList<>();
        this.dealerHand = new ArrayList<>();
        initializeDeck();
        gameLoop();
    }
    private void gameLoop(){
        int turn = 0;
        while(true){
            if(turn%2 == 0){
                playerTurn();
            }
            else{
                dealerTurn();
            }
            turn++;
        }
    }
    private void playerTurn(){
        return;
    }
    private void dealerTurn(){
        return;
    }

    private void initializeDeck() {
        String[] suits = {"♠", "♣", "♦", "♥"};
        String[] values = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};

        for (String suit : suits) {
            for (String value : values) {
                deck.add(new Card(suit, value));
            }
        }
        Collections.shuffle(deck);
    }

    public Card drawCard() {
        return deck.remove(0);
    }

    public void startGame() {
        playerHand.add(drawCard());
        playerHand.add(drawCard());
        dealerHand.add(drawCard());
        dealerHand.add(drawCard());
    }

    public List<Card> getPlayerHand() {
        return playerHand;
    }

    public List<Card> getDealerHand() {
        return dealerHand;
    }
}
