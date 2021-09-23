package com.lentera.silaq.EventBus;

import com.lentera.silaq.Model.PopularCategoryModel;

public class PopularCategoryClick {
    private PopularCategoryModel popularCategoryModel;

    public PopularCategoryClick(PopularCategoryModel popularCategoryModel) {
        this.popularCategoryModel = popularCategoryModel;
    }

    public PopularCategoryModel getPopularCategoryModel() {
        return popularCategoryModel;
    }

    public void setPopularCategoryModel(PopularCategoryModel popularCategoryModel) {
        this.popularCategoryModel = popularCategoryModel;
    }
}
