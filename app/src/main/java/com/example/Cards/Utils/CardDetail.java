package com.example.Cards.Utils;

import java.io.Serializable;

public class CardDetail implements Serializable {
    String cardName;
    int cardImage;

    public CardDetail(String cardName, int cardImage) {
        this.cardName = cardName;
        this.cardImage = cardImage;
    }

    public String getCardName() {
        return cardName;
    }

    public int getCardImage() {
        return cardImage;
    }
}
