package com.ashapiro.catanserver.game.util;

import com.ashapiro.catanserver.game.enums.Card;

@FunctionalInterface
public interface CardTransactionOperation {
    void execute(Card card) throws Exception;
}
