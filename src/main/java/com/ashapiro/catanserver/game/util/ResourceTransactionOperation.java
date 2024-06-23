package com.ashapiro.catanserver.game.util;

@FunctionalInterface
public interface ResourceTransactionOperation {
    void execute() throws Exception;
}
