package br.com.audora.shopping.mongodb.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

import br.com.audora.shopping.mongodb.models.Product;

public interface ProductRepository extends MongoRepository<Product, String>
{

    Product findByName(String name);

    @Override
    Optional<Product> findById(String id);
}
