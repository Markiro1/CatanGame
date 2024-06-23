package com.ashapiro.catanserver.game.util;

import com.ashapiro.catanserver.game.enums.Card;
import com.ashapiro.catanserver.game.enums.Resource;
import com.ashapiro.catanserver.game.model.Bank;
import com.ashapiro.catanserver.game.model.User;

import java.util.Map;

public class TransactionUtils {

    public static void executeWithRollback(User user, Bank bank, ResourceTransactionOperation operation) {
        Map<Resource, Integer> snapshotUserResourceInventory = user.getResourceInventory();
        Map<Resource, Integer> snapshotBankResourceStorage = bank.getResourceStorage();

        try {
            operation.execute();
        } catch (Exception e) {
            rollbackResourceTransaction(snapshotUserResourceInventory, snapshotBankResourceStorage, user, bank);
            System.out.println("Transaction failed and rolled back: " + e.getMessage());
        }
    }

    public static void executeWithRollback(User user, Bank bank, CardTransactionOperation operation) {
        Map<Card, Integer> snapshotUserCardInventory = user.getCardInventory();
        Map<Card, Integer> snapshotBankCardStorage = bank.getCardStorage();

        try {
            operation.execute(null);
        } catch (Exception e) {
            rollbackCardTransaction(snapshotUserCardInventory, snapshotBankCardStorage, user, bank);
            System.out.println("Transaction failed and rolled back: " + e.getMessage());
        }
    }

    public static void executeWithRollback(User robber, User victim, ResourceTransactionOperation operation) {
        Map<Resource, Integer> snapshotRobberResourceInventory = robber.getResourceInventory();
        Map<Resource, Integer> snapshotVictimResourceStorage = victim.getResourceInventory();

        try {
            operation.execute();
        } catch (Exception e) {
            rollbackResourceTransaction(snapshotRobberResourceInventory, snapshotVictimResourceStorage, robber, victim);
            System.out.println("Transaction failed and rolled back: " + e.getMessage());
        }
    }

    private static void rollbackCardTransaction(
            Map<Card, Integer> snapshotUserCardInventory,
            Map<Card, Integer> snapshotBankCardStorage,
            User user,
            Bank bank
    ) {
        user.setCardInventory(snapshotUserCardInventory);
        bank.setCardStorage(snapshotBankCardStorage);
    }

    private static void rollbackResourceTransaction(
            Map<Resource, Integer> snapshotUserResourceInventory,
            Map<Resource, Integer> snapshotBankResourceStorage,
            User user,
            Bank bank
    ) {
        user.setResourceInventory(snapshotUserResourceInventory);
        bank.setResourceStorage(snapshotBankResourceStorage);
    }

    private static void rollbackResourceTransaction(
            Map<Resource, Integer> snapshotRobberResourceInventory,
            Map<Resource, Integer> snapshotVictimResourceInventory,
            User robber,
            User victim
    ) {
        robber.setResourceInventory(snapshotRobberResourceInventory);
        victim.setResourceInventory(snapshotVictimResourceInventory);
    }
}
