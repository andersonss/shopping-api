package br.com.audora.shopping.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import br.com.audora.shopping.jpa.entities.CategoryEntity;

public interface CategoryJpaRepository extends JpaRepository<CategoryEntity, Long>
{
    List<CategoryEntity> findAllByName(String name);
}
