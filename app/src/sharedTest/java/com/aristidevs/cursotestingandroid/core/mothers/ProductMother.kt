package com.aristidevs.cursotestingandroid.core.mothers

import com.aristidevs.cursotestingandroid.core.builders.product

object ProductMother {
    fun bread(stock: Int = 8) =
        product {
            withId("id-bread")
            withName("Pan")
            withDescription("Calentito")
            withCategory("bread")
            withPrice(2.50)
            withStock(stock)
        }

    fun milk(stock: Int = 3) =
        product {
            withId("id-milk")
            withName("Leche")
            withDescription("Entera")
            withCategory("lacteo")
            withPrice(1.50)
            withStock(stock)
        }

    fun coffee(stock: Int = 2) =
        product {
            withId("id-coffee")
            withName("Café")
            withDescription("Americano")
            withCategory("drinks")
            withPrice(4.50)
            withStock(stock)
        }
}
