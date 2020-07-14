package br.com.audora.shopping.mongodb.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Document(collection = "products")
@TypeAlias("Product")
public class Product
{
    @Id
    private String id;

    private String name;

    private String description;

    private float price;

    private List<String> image_URLs;

    /*
    @DBRef
    private Client client;
     */

    private Set<EmbeddedCategory> fallIntoCategories;

    public Product() {
        image_URLs = new ArrayList<>();
        fallIntoCategories = new HashSet<>();
    }

    public Product(String name, String description, float price, HashSet<EmbeddedCategory> fallIntoCategories)
    {
        this();
        this.name = name;
        this.description = description;
        this.price = price;
        this.fallIntoCategories = fallIntoCategories;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public float getPrice()
    {
        return price;
    }

    public void setPrice(float price)
    {
        this.price = price;
    }

    public List<String> getImage_URLs()
    {
        return image_URLs;
    }

    public void setImage_URLs(List<String> image_URLs)
    {
        this.image_URLs = image_URLs;
    }

    public Set<EmbeddedCategory> getFallIntoCategories()
    {
        return fallIntoCategories;
    }

    public void setFallIntoCategories(HashSet<EmbeddedCategory> fallIntoCategories)
    {
        this.fallIntoCategories = fallIntoCategories;
    }
}

