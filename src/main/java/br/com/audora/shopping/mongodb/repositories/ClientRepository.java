package br.com.audora.shopping.mongodb.repositories;

import br.com.audora.shopping.mongodb.models.Client;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends MongoRepository<Client, String>
{
    @Query("{'profile.firstName': ?0}")
    List<Client> findByFirstName(String firstName);

    @Override
    Optional<Client> findById(String s);
}
