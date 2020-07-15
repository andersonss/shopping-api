package br.com.audora.shopping.mongodb.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "clients")
@TypeAlias(value = "Client")
public class Client
{
    @Id
    private String id;

    private Profile profile;

    private ShoppingCart shoppingCart;

    public Client()
    {
    }

    public Client(Profile profile)
    {
        this.profile = profile;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public Profile getProfile()
    {
        return profile;
    }

    public void setProfile(Profile profile)
    {
        this.profile = profile;
    }

    public ShoppingCart getShoppingCart() {
        return shoppingCart;
    }

    public void setShoppingCart(ShoppingCart shoppingCart) {
        this.shoppingCart = shoppingCart;
    }

    @Override
    public String toString()
    {
        if (profile == null)
        {
            return super.toString();
        }
        else
        {
            return profile.getFirstName() + " " + profile.getLastName();
        }
    }
}
