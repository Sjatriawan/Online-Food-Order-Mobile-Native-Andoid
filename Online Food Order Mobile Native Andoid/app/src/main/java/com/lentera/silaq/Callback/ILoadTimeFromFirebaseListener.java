package com.lentera.silaq.Callback;

import com.lentera.silaq.Model.Order;

public interface ILoadTimeFromFirebaseListener {
    void onLoadSuccess(Order order, long estimateTimeInMs);
    void onLoadFailed(String message);
}
