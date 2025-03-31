public class Card extends Parent {

    private static final int cWidth = 100;
    private static final int cHeight = 140;

    enum Suit {
        H, D, C, S;

        final Image image;
        Suit() {
            this.image = new Image(Card.class.getResourceAsStream("blackjack\src\main\java\com\blackjack\game\cards".concat(Rank.name()).concat(Suit.name()).concat(".png")),
                    32, 32, true, true);
        }
    }

    enum Rank {
        TWO(2), THREE(3), FOUR(4), FIVE(5), SIX(6), SEVEN(7), EIGHT(8), NINE(9), TEN(10),
        JACK(10), QUEEN(10), KING(10), ACE(11);

        final int value;
        Rank(int value) {
            this.value = value;
        }

        String displayName() {
            return ordinal() < 9 ? String.valueOf(value) : name().substring(0, 1);
        }
    }

    public Suit suit;
    public Rank rank;
    public int value;

    public Card(Suit suit, Rank rank) {
        this.suit = suit;
        this.rank = rank;
        this.value = rank.value;

        Rectangle bg = new Rectangle(cWidth, cHeight);
        bg.setArcWidth(20);
        bg.setArcHeight(20);
        bg.setFill(Color.WHITE);

        Text text1 = new Text(rank.displayName());
        text1.setFont(Font.font(18));
        text1.setX(cWidth - text1.getLayoutBounds().getWidth() - 10);
        text1.setY(text1.getLayoutBounds().getHeight());

        Text text2 = new Text(text1.getText());
        text2.setFont(Font.font(18));
        text2.setX(10);
        text2.setY(cHeight - 10);

        ImageView view = new ImageView(suit.image);
        view.setRotate(180);
        view.setX(cWidth - 32);
        view.setY(cHeight - 32);

        getChildren().addAll(bg, new ImageView(suit.image), view, text1, text2);
    }

    @Override
    public String toString() {
        return rank.toString() + " of " + suit.toString();
    }
}