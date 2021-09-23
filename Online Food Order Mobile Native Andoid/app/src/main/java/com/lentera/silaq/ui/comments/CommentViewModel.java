package com.lentera.silaq.ui.comments;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lentera.silaq.Model.CommentModel;
import com.lentera.silaq.Model.FoodModel;

import java.util.List;

public class CommentViewModel extends ViewModel {
    private MutableLiveData<List<CommentModel>> mutableLiveDataFoodList;

    public CommentViewModel() {
        mutableLiveDataFoodList = new MutableLiveData<>();
    }

    public MutableLiveData<List<CommentModel>> getMutableLiveDataFoodList() {
        return mutableLiveDataFoodList;
    }
    public void setCommenList (List<CommentModel> commenList){
        mutableLiveDataFoodList.setValue(commenList);
    }
}
