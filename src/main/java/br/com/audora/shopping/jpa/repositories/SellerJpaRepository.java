package br.com.audora.shopping.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.audora.shopping.jpa.entities.SellerEntity;

public interface SellerJpaRepository extends JpaRepository<SellerEntity, Long>
{

}
