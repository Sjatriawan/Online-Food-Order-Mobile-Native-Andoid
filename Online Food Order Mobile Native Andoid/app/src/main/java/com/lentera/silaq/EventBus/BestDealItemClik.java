package com.lentera.silaq.EventBus;

import com.lentera.silaq.Model.BestDealModel;

public class BestDealItemClik {
    private BestDealModel bestDealModel;

    public BestDealItemClik(BestDealModel bestDealModel) {
        this.bestDealModel = bestDealModel;
    }

    public BestDealModel getBestDealModel() {
        return bestDealModel;
    }

    public void setBestDealModel(BestDealModel bestDealModel) {
        this.bestDealModel = bestDealModel;
    }
}
