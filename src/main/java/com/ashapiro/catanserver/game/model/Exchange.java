package com.ashapiro.catanserver.game.model;

import com.ashapiro.catanserver.game.enums.ExchangeStatus;
import com.ashapiro.catanserver.game.enums.Resource;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Exchange {

    private static Long counter = 1L;

    private Long exchangeId;

    private User initiatorUser;

    private User targetUser;

    private int targetAmountOfResource;

    private int initiatorAmountOfResource;

    private Resource initiatorResource;

    private Resource targetResource;

    private ExchangeStatus status;

    public Exchange(User initiatorUser,
                    User targetUser,
                    int targetAmountOfResource,
                    int initiatorAmountOfResource,
                    Resource initiatorResource,
                    Resource targetResource
    ) {
        this.exchangeId = counter++;
        this.initiatorUser = initiatorUser;
        this.targetUser = targetUser;
        this.targetAmountOfResource = targetAmountOfResource;
        this.initiatorAmountOfResource = initiatorAmountOfResource;
        this.initiatorResource = initiatorResource;
        this.targetResource = targetResource;
        this.status = ExchangeStatus.WAITING;
    }
}
