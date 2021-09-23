package com.lentera.silaq.Callback;

import com.lentera.silaq.Model.BestDealModel;
import com.lentera.silaq.Model.PlusModel;

import java.util.List;

public interface IPlusCallbackListener {
    void onPlusLoadSuccess(List<PlusModel> plusModels);
    void onPlusLoadFailed(String message);

}
