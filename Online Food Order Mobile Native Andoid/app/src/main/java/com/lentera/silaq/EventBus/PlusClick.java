package com.lentera.silaq.EventBus;

import com.lentera.silaq.Model.PlusModel;

public class PlusClick {
    private PlusModel plusModel;

    public PlusClick(PlusModel plusModel) {
        this.plusModel = plusModel;
    }

    public PlusModel getPlusModel() {
        return plusModel;
    }

    public void setPlusModel(PlusModel plusModel) {
        this.plusModel = plusModel;
    }
}
