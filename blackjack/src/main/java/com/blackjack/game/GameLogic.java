package com.blackjack.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameLogic {
    public List<Card> deck;
    public List<Card> playerHand;
    public List<Card> dealerHand;
    @SuppressWarnings("FieldMayBeFinal")
    private Player player; 

    public GameLogic(Player player) {
        this.deck = new ArrayList<>();
        this.playerHand = new ArrayList<>(); 
        this.dealerHand = new ArrayList<>();
        this.player = player; 
        initializeDeck();
    }

    private void initializeDeck() {
        String[] suits = {"S", "C", "D", "H"};
        String[] values = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};

        for (String suit : suits) {
            for (String value : values) {
                deck.add(new Card(suit, value));
            }
        }
        Collections.shuffle(deck);
    }

    public Card drawCard() {
        if (deck.isEmpty()) {
            
            System.out.println("Reshuffling deck...");
            initializeDeck();
        }
        return deck.remove(0);
    }

    public void startGame() {
        Card playerCard1 = drawCard();
        Card playerCard2 = drawCard();
        Card dealerCard1 = drawCard();
        Card dealerCard2 = drawCard();

        playerHand.add(playerCard1);
        playerHand.add(playerCard2);
        dealerHand.add(dealerCard1);
        dealerHand.add(dealerCard2);

        
        player.setHand(new ArrayList<>(playerHand)); 
    }

    public Card playerHit() {
        Card newCard = drawCard();
        playerHand.add(newCard);
        // Optionally update the Player's hand as well
        player.getHand().add(newCard);
        return newCard;
    }

    public List<Card> dealerPlay() {
        while (calculateHandValue(dealerHand) < 17) {
            dealerHand.add(drawCard());
        }
        return dealerHand;
    }

    public int calculateHandValue(List<Card> hand) {
        int sum = 0;
        int numAces = 0;
        for (Card card : hand) {
            int value = card.getValue();
            sum += value > 10 ? 10 : value; 
            if (card.getRank().equals("A")) {
                numAces++;
            }
        }

        while (sum > 21 && numAces > 0) {
            sum -= 10; 
            numAces--;
        }
        return sum;
    }

    public List<Card> getPlayerHand() {
        return Collections.unmodifiableList(playerHand); 
    }

    public List<Card> getDealerHand() {
        return Collections.unmodifiableList(dealerHand); 
    }

    // Method to check for Blackjack
    public boolean checkBlackjack(List<Card> hand) {
        if (hand.size() == 2) {
            return (hand.get(0).getValue() == 11 && hand.get(1).getValue() == 10) ||
                   (hand.get(0).getValue() == 10 && hand.get(1).getValue() == 11);
        }
        return false;
    }

    public String determineWinner() {
        int playerValue = calculateHandValue(playerHand);
        int dealerValue = calculateHandValue(dealerHand);

        if (checkBlackjack(playerHand) && !checkBlackjack(dealerHand)) {
            return "Player Blackjack!";
        } else if (!checkBlackjack(playerHand) && checkBlackjack(dealerHand)) {
            return "Dealer Blackjack!";
        } else if (playerValue > 21) {
            return "Player Busts! Dealer Wins.";
        } else if (dealerValue > 21) {
            return "Dealer Busts! Player Wins.";
        } else if (playerValue > dealerValue) {
            return "Player Wins!";
        } else if (dealerValue > playerValue) {
            return "Dealer Wins!";
        } else {
            return "Push!"; 
        }
    }
}