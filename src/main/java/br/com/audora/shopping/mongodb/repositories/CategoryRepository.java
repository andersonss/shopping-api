package br.com.audora.shopping.mongodb.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import br.com.audora.shopping.mongodb.models.Category;

public interface CategoryRepository extends MongoRepository<Category, String>
{
    Category findByName(String categoryName);
}
