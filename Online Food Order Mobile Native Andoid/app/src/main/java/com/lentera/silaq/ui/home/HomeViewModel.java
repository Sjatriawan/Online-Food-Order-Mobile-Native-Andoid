package com.lentera.silaq.ui.home;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lentera.silaq.Callback.IBestDealCallbackListener;
import com.lentera.silaq.Callback.IPlusCallbackListener;
import com.lentera.silaq.Callback.IPopularCallbackListener;
import com.lentera.silaq.Common.Common;
import com.lentera.silaq.Model.BestDealModel;
import com.lentera.silaq.Model.PlusModel;
import com.lentera.silaq.Model.PopularCategoryModel;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends ViewModel implements IPopularCallbackListener, IBestDealCallbackListener, IPlusCallbackListener {
    private MutableLiveData<List<PopularCategoryModel>> popularList;
    private MutableLiveData<List<BestDealModel>> bestDealList;
    private MutableLiveData<List<PlusModel>> plusList;
    private MutableLiveData<String> messageError;
    private IPopularCallbackListener popularCallbackListener;
    private IBestDealCallbackListener bestDealCallbackListener;
    private IPlusCallbackListener iPlusCallbackListener;


    public HomeViewModel() {
        popularCallbackListener = this;
        bestDealCallbackListener= this;
        iPlusCallbackListener = this;
    }

    public MutableLiveData<List<BestDealModel>> getBestDealList() {
        if(bestDealList == null){
            bestDealList = new MutableLiveData<>();
            messageError = new MutableLiveData<>();
            loadBestDealList();

        }
        return bestDealList;
    }

    private void loadBestDealList() {
        List<BestDealModel> tempList = new ArrayList<>();
        DatabaseReference bestDealRef = FirebaseDatabase.getInstance().getReference(Common.BEST_DEAL_REF);
        bestDealRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot itemSnapshot:snapshot.getChildren()){
                    BestDealModel model = itemSnapshot.getValue(BestDealModel.class);
                    tempList.add(model);
                }
                bestDealCallbackListener.onBestDealLoadSuccess(tempList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                bestDealCallbackListener.onBestDealLoadFailed(error.getMessage());

            }
        });
    }


    public MutableLiveData<List<PlusModel>> getPlusList() {
       if(plusList == null){
           plusList = new MutableLiveData<>();
           messageError = new MutableLiveData<>();
           loadPlusList();
       }
       return plusList;
    }

    private void loadPlusList() {
        List<PlusModel> tempList = new ArrayList<>();
        DatabaseReference plusRef= FirebaseDatabase.getInstance().getReference(Common.PLUS_REF);
        plusRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot itemSnapshot:snapshot.getChildren()){
                    PlusModel model = itemSnapshot.getValue(PlusModel.class);
                    tempList.add(model);
                }
                iPlusCallbackListener.onPlusLoadSuccess(tempList);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                popularCallbackListener.onPopularLoadFailed(error.getMessage());

            }
        });
    }



    public MutableLiveData<List<PopularCategoryModel>> getPopularList() {
        if(popularList == null){
            popularList = new MutableLiveData<>();
            messageError = new MutableLiveData<>();
            loadPopularList();
        }
        return popularList;
    }

    private void loadPopularList() {
        List<PopularCategoryModel> tempList = new ArrayList<>();
        DatabaseReference popularRef= FirebaseDatabase.getInstance().getReference(Common.POPULAR_CATEGORY_REF);
        popularRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot itemSnapshot:snapshot.getChildren()){
                    PopularCategoryModel model = itemSnapshot.getValue(PopularCategoryModel.class);
                    tempList.add(model);
                }
                popularCallbackListener.onPopularLoadSuccess(tempList);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                popularCallbackListener.onPopularLoadFailed(error.getMessage());

            }
        });
    }


    public MutableLiveData<String> getMessageError() {
        return messageError;
    }

    @Override
    public void onPopularLoadSuccess(List<PopularCategoryModel> popularCategoryModels) {
        popularList.setValue(popularCategoryModels);
    }

    @Override
    public void onPopularLoadFailed(String message) {
        messageError.setValue(message);

    }

    @Override
    public void onBestDealLoadSuccess(List<BestDealModel> bestDealModels) {
        bestDealList.setValue(bestDealModels);

    }

    @Override
    public void onBestDealLoadFailed(String message) {
        messageError.setValue(message);

    }


    @Override
    public void onPlusLoadSuccess(List<PlusModel> plusModels) {
        plusList.setValue(plusModels);
    }

    @Override
    public void onPlusLoadFailed(String message) {
        messageError.setValue(message);
    }
}