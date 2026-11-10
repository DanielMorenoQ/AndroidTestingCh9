package com.example.sdgkuandroidtestingch8.shopping

import com.example.sdgkuandroidtestingch8.ShoppingCart
import com.example.sdgkuandroidtestingch8.models.Product

class ShopViewModel(
    private val productRepository: ProductRepository,
    private val shoppingCart: ShoppingCart = ShoppingCart()
) {
    val products get() = productRepository.getProducts()

    fun addToCart(product: Product) {
        shoppingCart.addItem(product)
    }

    fun subtotalPrice(): Double {
        return shoppingCart.totalPrice()
    }
}
