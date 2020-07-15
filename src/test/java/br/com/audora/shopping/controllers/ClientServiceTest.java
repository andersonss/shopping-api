package br.com.audora.shopping.controllers;

import br.com.audora.shopping.enums.Gender;
import br.com.audora.shopping.mongodb.models.Client;
import br.com.audora.shopping.mongodb.models.Profile;
import br.com.audora.shopping.mongodb.repositories.ClientRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.fest.assertions.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ClientServiceTest {

    public static final String rootUrl = "http://localhost:";

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ClientRepository clientMongoRepository;

    @Test
    public void shouldCreateClient() {
        Profile profile = new Profile("Billy", "Sandey", Gender.Male);
        HttpEntity<Client> request = new HttpEntity<>(new Client(profile));
        Client client = restTemplate.postForObject(rootUrl + this.port + "/client", request, Client.class);
        assertThat(client).isNotNull();
        assertThat(client.getProfile().getFirstName()).isEqualTo("Billy");
    }

    @Test
    public void shouldGetAllClients() {
        ResponseEntity<String> response = this.restTemplate.getForEntity(rootUrl + this.port + "/client/all",
                String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void shouldUpdateClient() {
        Profile profile = new Profile("Billy", "Sandey", Gender.Male);
        Client client = new Client(profile);
        Client createdClient = clientMongoRepository.save(client);
        Client updateClient = new Client(new Profile("Anderson", "Silva", Gender.Male));
        updateClient.setId(createdClient.getId());
        HttpEntity<Client> request = new HttpEntity<>(updateClient);
        restTemplate.put(rootUrl + this.port + "/client", request);
        Optional<Client> byId = clientMongoRepository.findById(updateClient.getId());
        assertThat(updateClient.getId()).isEqualTo(byId.get().getId());
    }
}

