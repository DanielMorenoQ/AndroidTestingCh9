package com.example.sdgkuandroidtestingch8

import com.example.sdgkuandroidtestingch8.models.Product
import org.junit.Before
import org.junit.Test

/**
 * TDD Exercise: Shopping Cart
 * 1. Create a ShoppingCart
 * 2. When the shopping cart is empty, the size should be 0
 * 3. When the shopping cart is empty the total price should be 0.0
 * 4. When an item is added to the shopping cart, the size should be 1
 * 5. When items are added to the shopping cart, the total price should be X
 * 6. When an item is removed from the shopping cart, the size should decrease by 1
 * 7. When the total is greater than 200.0, a discount of 10% should be applied
 * 8. When the total is greater than 500.0, a discount of 20% should be applied
 */
class ShoppingCartTests {
    var cart = ShoppingCart()

    @Before
    fun setup() {
        // ARRANGE
        cart = ShoppingCart()
    }

    @Test
    fun shoppingCart_isEmpty() {
        // ACT
        val size = cart.size()

        // ASSERT
        assert(size == 0)
    }

    @Test
    fun shoppingCart_totalPriceIsZero() {
        // ACT
        val totalPrice = cart.totalPrice()

        // ASSERT
        assert(totalPrice == 0.0)
    }

    @Test
    fun whenAddingItemToShoppingCart_sizeIncreases() {
        // ARRANGE
        val product = Product("Item 1", 10.0)

        // ACT
        cart.addItem(product)

        // ASSERT
        assert(cart.size() == 1)
    }

    @Test
    fun whenAddingItemsToShoppingCart_totalPriceIncreases() {
        // ARRANGE
        val product1 = Product("Item 1", 10.0)
        val product2 = Product("Item 2", 20.0)

        // ACT
        cart.addItem(product1)
        cart.addItem(product2)

        // ASSERT
        assert(cart.totalPrice() == 30.0)
    }

    @Test
    fun whenItemIsRemovedFromShoppingCart_sizeDecreases() {
        // ARRANGE
        val product1 = Product("Item 1", 10.0)
        val product2 = Product("Item 2", 20.0)

        cart.addItem(product1)
        cart.addItem(product2)

        // ACT
        cart.removeItem(product1)

        // ASSERT
        assert(cart.size() == 1)
    }

    @Test
    fun whenTotalPriceIsGreaterThan200_10PercentDiscountIsApplied() {
        // ARRANGE
        val product1 = Product("Item 1", 100.0)
        val product2 = Product("Item 2", 150.0)

        cart.addItem(product1)
        cart.addItem(product2)

        // ACT
        val totalPrice = cart.totalPrice()

        // ASSERT
        assert(totalPrice == 225.0)
    }

    @Test
    fun whenTotalPriceIsGreaterThan500_20PercentDiscountIsApplied() {
        // ARRANGE
        val product1 = Product("Item 1", 300.0)
        val product2 = Product("Item 2", 300.0)

        cart.addItem(product1)
        cart.addItem(product2)

        // ACT
        val totalPrice = cart.totalPrice()

        // ASSERT
        assert(totalPrice == 480.0)
    }
}