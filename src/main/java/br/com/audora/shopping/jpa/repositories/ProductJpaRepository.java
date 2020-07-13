package br.com.audora.shopping.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.audora.shopping.jpa.entities.ProductEntity;

public interface ProductJpaRepository extends JpaRepository<ProductEntity, Long>
{
    ProductEntity findByName(String name);
}
