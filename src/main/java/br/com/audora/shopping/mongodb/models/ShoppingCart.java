package br.com.audora.shopping.mongodb.models;

import org.springframework.format.annotation.DateTimeFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ShoppingCart {

    private List<Product> orderProducts;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private Date dateCreated;

    private float totalPrice;

    private float totalDiscount;

    private float finalPrice;

    public ShoppingCart() {
        orderProducts = new ArrayList<>();
        dateCreated = new Date();
    }

    public void calculatePrice() {
        float totalPrice = 0;
        float totalDiscount = 0;
        for (Product orderProduct : orderProducts) {
            totalPrice += orderProduct.getPrice();
            float discountPercentage = orderProduct.getDiscountPercentage();
            if (discountPercentage > 0) {
                totalDiscount += orderProduct.getPrice() * (discountPercentage / 100);
            }
        }
        setTotalPrice(totalPrice);
        setTotalDiscount(totalDiscount);
        setFinalPrice(totalPrice - totalDiscount);

    }

    public int getNumberOfProducts() {
        return this.orderProducts.size();
    }

    public void addOrderProduct(Product orderProduct) {
        orderProducts.add(orderProduct);
        calculatePrice();
    }

    public List<Product> getOrderProducts() {
        return orderProducts;
    }

    public void setOrderProducts(List<Product> orderProducts) {
        this.orderProducts = orderProducts;
        calculatePrice();
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public float getTotalPrice() {
        return totalPrice;
    }

    public float getTotalDiscount() {
        return totalDiscount;
    }

    public void setTotalPrice(float totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void setTotalDiscount(float totalDiscount) {
        this.totalDiscount = totalDiscount;
    }

    public float getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(float finalPrice) {
        this.finalPrice = finalPrice;
    }
}
