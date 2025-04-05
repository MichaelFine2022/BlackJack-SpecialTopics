package com.blackjack.game;
import java.util.ArrayList;
import java.util.List;
public class Player {
    private String name;
    private List<Card> hand;
    private int score;
    private int aceCount=0;

    public Player(String name) {
        this.name = name;
        this.hand = new ArrayList<>();
    }
    public void receiveCard(Card card) {
        hand.add(card);
        updateScore();
    }

    private void updateScore() {
        Card recent = hand.get(hand.size() - 1);
        if(recent.getValue() != 0){
            score+=recent.getValue();
        }
        else{
            aceCount++;
        }

    }

    public int getScore() { return score; }
    public String getName() { return name; }
    public List<Card> getHand() { return hand; }
    public int getAces(){ return aceCount; }
}
