package com.ashapiro.catanserver.game.model;

import com.ashapiro.catanserver.game.MapUtil;
import com.ashapiro.catanserver.game.enums.Buff;
import com.ashapiro.catanserver.game.enums.Card;
import com.ashapiro.catanserver.game.enums.Resource;
import com.ashapiro.catanserver.game.enums.VertexBuildingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {

    private Long id;

    private String name;

    private Boolean isReady;

    private Socket socket;

    private boolean hisTurn;

    private boolean isHaveLongestRoad;

    private boolean isHaveLargestArmy;

    private int usedKnightCardCount;

    private int longestRoadLength;

    private Map<Resource, Integer> resourceInventory;

    private Map<Card, Integer> cardInventory;

    private Map<Buff, Integer> buffs;

    public User(Long id, String name, Socket socket) {
        this.id = id;
        this.name = name;
        this.socket = socket;
        this.usedKnightCardCount = 0;
        this.longestRoadLength = 0;
        this.isReady = false;
        this.isHaveLongestRoad = false;
        this.isHaveLargestArmy = false;
        this.resourceInventory = new HashMap<>();
        this.cardInventory = new HashMap<>();
        this.buffs = new HashMap<>();
    }

    public int getUserVictoryPoints(List<Vertex> vertices) {
        int victoryPoint = 0;
        List<Vertex> userVertices = MapUtil.getUserVertices(this, vertices);
        for (Vertex vertex : userVertices) {
            if (vertex.getType().equals(VertexBuildingType.SETTLEMENT)) {
                victoryPoint++;
            } else if (vertex.getType().equals(VertexBuildingType.CITY)) {
                victoryPoint += 2;
            }
        }
        victoryPoint += this.isHaveLongestRoad ? 2 : 0;
        victoryPoint += this.isHaveLargestArmy ? 2 : 0;
        victoryPoint += this.cardInventory.getOrDefault(Card.VICTORY_POINT, 0);
        return victoryPoint;
    }

    public void stealFromAnotherUser(User victim, Resource resource) {
        int victimResourceAmount = victim.getResourceInventory().getOrDefault(resource, 0);
        int thiefResourceAmount = this.getResourceInventory().getOrDefault(resource, 0);
        victim.getResourceInventory().put(resource, victimResourceAmount - 1);
        this.getResourceInventory().put(resource, thiefResourceAmount + 1);
    }

    public void retrieveResourceFromInventory(Resource resource, int amount) {
        int currentAmount = this.resourceInventory.getOrDefault(resource, 0);
        if (currentAmount >= amount) {
            this.resourceInventory.put(resource, currentAmount - amount);
        }
    }

    public void addResourceToInventory(Resource resource, int amount) {
        int currentAmount = resourceInventory.getOrDefault(resource, 0);
        this.resourceInventory.put(resource, currentAmount + amount);
    }

    public void addCardToInventory(Card card, int amount) {
        int currentAmount = cardInventory.getOrDefault(card, 0);
        cardInventory.put(card, currentAmount + amount);
    }

    public void addBuff(Buff buff) {
        switch (buff) {
            case ROAD_BUILDING -> buffs.put(buff, 2);
        }
    }

    public void removeBuff(Buff buff, int amount) {
        int currentAmount = buffs.getOrDefault(buff, 0);
        if (currentAmount >= amount) {
            buffs.put(buff, currentAmount - amount);
        }
    }

    public void retrieveCardFromInventory(Card card, int amount) {
        int currentAmount = cardInventory.getOrDefault(card, 0);
        if (currentAmount >= amount) {
            cardInventory.put(card, currentAmount - amount);
        }
    }

    public boolean containsCard(Card card) {
        return cardInventory.containsKey(card);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        User user = (User) object;
        return Objects.equals(id, user.id) && Objects.equals(name, user.name) && Objects.equals(socket, user.socket);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, socket);
    }
}
