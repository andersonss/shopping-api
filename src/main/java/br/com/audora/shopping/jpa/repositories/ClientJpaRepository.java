package br.com.audora.shopping.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.audora.shopping.jpa.entities.ClientEntity;

public interface ClientJpaRepository extends JpaRepository<ClientEntity, Long>
{

}
