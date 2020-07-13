package br.com.audora.shopping.jpa.entities;


import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "sellers")
public class ClientEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String accountId;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "seller", orphanRemoval = true)
    private ProfileEntity profile;

    public ClientEntity()
    {
    }

    public ClientEntity(String accountId)
    {
        this.accountId = accountId;
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String getAccountId()
    {
        return accountId;
    }

    public void setAccountId(String accountId)
    {
        this.accountId = accountId;
    }

    public ProfileEntity getProfile()
    {
        return profile;
    }

    public void setProfile(ProfileEntity profile)
    {
        this.profile = profile;
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
            return getProfile().getFirstName() + " " + getProfile().getLastName();
        }
    }
}

