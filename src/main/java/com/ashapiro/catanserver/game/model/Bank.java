package com.ashapiro.catanserver.game.model;

import com.ashapiro.catanserver.game.enums.Card;
import com.ashapiro.catanserver.game.enums.Resource;
import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class Bank {

    private Map<Resource, Integer> resourceStorage;

    private Map<Card, Integer> cardStorage;

    public Bank() {
        this.resourceStorage = new ConcurrentHashMap<>();
        this.cardStorage = new ConcurrentHashMap<>();
        initializeResourceStorage(this.resourceStorage);
        initializeCardStorage(this.cardStorage);
    }

    public void addResourceToStorage(Resource resource, int amount) {
        int currentAmount = resourceStorage.getOrDefault(resource, 0);
        resourceStorage.put(resource, currentAmount + amount);
    }

    public void retrieveResourceFromStorage(Resource resource, int amount) {
        int currentAmount = resourceStorage.getOrDefault(resource, 0);
        if (currentAmount >= amount) {
            resourceStorage.put(resource, currentAmount - amount);
        }
    }

    public boolean retrieveCardFromStorage(Card card, int amount) {
        int currentAmount = cardStorage.getOrDefault(card, 0);
        if (currentAmount >= amount) {
            cardStorage.put(card, currentAmount - amount);
            return true;
        }
        return false;
    }

    private void initializeResourceStorage(Map<Resource, Integer> storage) {
        for (Resource resource : Resource.values()) {
            storage.put(resource, 20);
        }
    }

    private void initializeCardStorage(Map<Card, Integer> storage) {
        for (Card card : Card.values()) {
            switch (card) {
                case VICTORY_POINT -> storage.put(card, 5);
                case KNIGHT -> storage.put(card, 14);
                case ROAD_BUILDING, YEAR_OF_PLENTY, MONOPOLY -> storage.put(card, 2);
            }
        }
    }
}