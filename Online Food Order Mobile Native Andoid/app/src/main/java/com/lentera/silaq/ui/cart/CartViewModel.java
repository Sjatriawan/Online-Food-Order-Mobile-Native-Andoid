package com.lentera.silaq.ui.cart;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lentera.silaq.Common.Common;
import com.lentera.silaq.Database.CartDataSource;
import com.lentera.silaq.Database.CartDatabase;
import com.lentera.silaq.Database.CartItem;
import com.lentera.silaq.Database.LocalCartDataSource;
import com.lentera.silaq.Model.CommentModel;
import com.lentera.silaq.Model.FoodModel;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class CartViewModel extends ViewModel {
    private MutableLiveData<List<CartItem>> mutableLiveDataCartItems;
    private CompositeDisposable compositeDisposable;
    private CartDataSource cartDataSource;

    public CartViewModel(){
        compositeDisposable = new CompositeDisposable();
    }

    public void initCartDataSource(Context context){
        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(context).cartDAO());
    }
    public void onStop(){
        compositeDisposable.clear();
    }

    public MutableLiveData<List<CartItem>> getMutableLiveDataCartItems() {
        if(mutableLiveDataCartItems == null)
            mutableLiveDataCartItems = new MutableLiveData<>();
        getAllCartItems();
        return mutableLiveDataCartItems;
    }

    private void getAllCartItems() {
        compositeDisposable.add(cartDataSource.getAllCart(Common.currentUser.getUid())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(cartItems -> {
            mutableLiveDataCartItems.setValue(cartItems);
        }, throwable -> {
            mutableLiveDataCartItems.setValue(null);
        }));
    }
}