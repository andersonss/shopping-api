package br.com.audora.shopping.mongodb.repositories;

import br.com.audora.shopping.mongodb.models.Seller;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SellerRepository extends MongoRepository<Seller, String>
{
    @Query("{'profile.firstName': ?0}")
    List<Seller> findByFirstName(String firstName);

    @Override
    Optional<Seller> findById(String s);
}
