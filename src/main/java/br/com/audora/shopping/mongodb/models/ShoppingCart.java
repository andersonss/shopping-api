package br.com.audora.shopping.mongodb.models;

import org.springframework.data.annotation.Id;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ShoppingCart {

    @Id
    private String id;

    private List<Product> orderProducts;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private Date dateCreated;

    private float totalPrice;

    public ShoppingCart() {
        orderProducts = new ArrayList<>();
    }

    public float getTotalPrice() {
        float totalPrice = 0;
        for (Product orderProduct : orderProducts) {
            totalPrice += orderProduct.getPrice();
        }
        return totalPrice;
    }

    public int getNumberOfProducts() {
        return this.orderProducts.size();
    }

    public void addOrderProduct(Product orderProduct) {
        orderProducts.add(orderProduct);
    }
}
