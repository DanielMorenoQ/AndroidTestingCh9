package com.example.sdgkuandroidtestingch8

import com.example.sdgkuandroidtestingch8.models.Product

class ShoppingCart {
    val products = mutableListOf<Product>()

    fun addItem(product: Product) {
        products.add(product)
    }

    fun totalPrice(): Double {
        val totalValue =  products.sumOf {
            it.price
        }
        return when {
            totalValue > 500.0 -> totalValue * 0.8
            totalValue > 200.0 -> totalValue * 0.9
            else -> totalValue
        }
    }

    fun removeItem(product: Product) {
        products.remove(product)
    }

    fun size(): Int {
        return products.size
    }
}