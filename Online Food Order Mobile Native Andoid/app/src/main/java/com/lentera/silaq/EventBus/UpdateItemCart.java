package com.lentera.silaq.EventBus;

import com.lentera.silaq.Database.CartItem;

public class UpdateItemCart {
    private CartItem cartItem;

    public UpdateItemCart(CartItem cartItem) {
        this.cartItem = cartItem;
    }

    public CartItem getCartItem() {
        return cartItem;
    }

    public void setCartItem(CartItem cartItem) {
        this.cartItem = cartItem;
    }
}
