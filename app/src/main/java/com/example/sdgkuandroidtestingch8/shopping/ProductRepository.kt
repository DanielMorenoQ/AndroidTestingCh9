package com.example.sdgkuandroidtestingch8.shopping

import com.example.sdgkuandroidtestingch8.models.Product

interface ProductRepository {
    fun getProducts(): List<Product>
}

class InMemoryProductRepository : ProductRepository {
    override fun getProducts(): List<Product> {
        // Implementation goes here
        return emptyList()
    }
}