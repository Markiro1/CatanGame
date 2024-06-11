package com.ashapiro.catanserver.game;

import com.ashapiro.catanserver.game.enums.Resource;

import java.util.HashMap;
import java.util.Map;

public class ResourceCost {

    public static Map<Resource, Integer> getRoadCost() {
        Map<Resource, Integer> roadCost = new HashMap<>();
        roadCost.put(Resource.LUMBER, 1);
        roadCost.put(Resource.BRICK, 1);
        return roadCost;
    }

    public static Map<Resource, Integer> getSettlementCost() {
        Map<Resource, Integer> settlementCost = new HashMap<>();
        settlementCost.put(Resource.LUMBER, 1);
        settlementCost.put(Resource.BRICK, 1);
        settlementCost.put(Resource.GRAIN, 1);
        settlementCost.put(Resource.WOOL, 1);
        return settlementCost;
    }

    public static Map<Resource, Integer> getCityCost() {
        Map<Resource, Integer> cityCost = new HashMap<>();
        cityCost.put(Resource.GRAIN, 2);
        cityCost.put(Resource.ORE, 3);
        return cityCost;
    }
}
