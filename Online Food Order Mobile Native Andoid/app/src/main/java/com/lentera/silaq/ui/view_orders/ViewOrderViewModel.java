package com.lentera.silaq.ui.view_orders;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lentera.silaq.Callback.ICategoryCallbackListener;
import com.lentera.silaq.Common.Common;
import com.lentera.silaq.Model.CategoryModel;
import com.lentera.silaq.Model.Order;

import java.util.ArrayList;
import java.util.List;

public class ViewOrderViewModel extends ViewModel {
    private MutableLiveData<List<Order>> mutableLiveDataOrderList;
    public ViewOrderViewModel(){
        mutableLiveDataOrderList = new MutableLiveData<>();
    }

    public MutableLiveData<List<Order>> getMutableLiveDataOrderList() {
        return mutableLiveDataOrderList;
    }

    public void setMutableLiveDataOrderList(List<Order> orderList) {
       mutableLiveDataOrderList.setValue(orderList);
    }
}