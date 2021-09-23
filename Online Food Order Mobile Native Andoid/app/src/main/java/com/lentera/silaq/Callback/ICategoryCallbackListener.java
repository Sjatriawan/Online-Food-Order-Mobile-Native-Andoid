package com.lentera.silaq.Callback;

import com.lentera.silaq.Model.CategoryModel;

import java.util.Calendar;
import java.util.List;

public interface ICategoryCallbackListener {
    void onCategoryLoadSuccess(List<CategoryModel> categoryModelList);
    void onCategoryLoadFailed(String message);

}
