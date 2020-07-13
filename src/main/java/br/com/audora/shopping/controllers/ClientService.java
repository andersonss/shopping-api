package br.com.audora.shopping.controllers;

import com.mongodb.MongoClient;
import com.mongodb.client.result.UpdateResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;

import br.com.audora.shopping.jpa.entities.ProfileEntity;
import br.com.audora.shopping.jpa.entities.ClientEntity;
import br.com.audora.shopping.jpa.repositories.ClientJpaRepository;
import br.com.audora.shopping.mongodb.models.Profile;
import br.com.audora.shopping.mongodb.models.Client;
import br.com.audora.shopping.mongodb.repositories.ClientRepository;

@RestController
@RequestMapping(path = "/client")
public class ClientService
{
    private MongoOperations _mongoOperation = new MongoTemplate(new MongoClient(), "local");
    @Autowired
    private ClientJpaRepository _clientJpaRepository;
    @Autowired
    private ClientRepository _clientMongoRepository;


    //----------Retrieve Clients----------------
    @GetMapping(path = "/mongo")
    public ResponseEntity<?> getClientsFromMongoDB(@RequestParam(value = "firstName") String firstName)
    {
        List<Client> clients = _clientMongoRepository.findByFirstName(firstName);
        if (clients.size() > 0)
        {
            System.out.println("There are " + clients.size() + " sellers with first name " + firstName + " in MongoDB database.");
            return new ResponseEntity<>(clients, HttpStatus.OK);
        }
        return new ResponseEntity<>("There isn't any seller with this name in MongoDB.", HttpStatus.NOT_FOUND);
    }

    @GetMapping(path = "/all/mongo")
    public List<Client> getAllClientsFromMongoDB()
    {
        return _clientMongoRepository.findAll();
    }

    @GetMapping(path = "/mysql")
    public ResponseEntity<?> getClientFromMysql(@RequestParam(value = "id") long id)
    {
        try
        {
            ClientEntity client = _clientJpaRepository.findById(id).orElseThrow(EntityNotFoundException::new);
            System.out.println("The seller with id " + id + " = " + client.toString());
            return new ResponseEntity<>(client, HttpStatus.OK);
        }
        catch (EntityNotFoundException e)
        {
            return new ResponseEntity<>("There isn't any seller with this name in MySQL.", HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(path = "/all/mysql")
    public List<ClientEntity> getAllSellersFromMysql()
    {
        return _clientJpaRepository.findAll();
    }


    //----------Create a Client-----------------
    @PostMapping(path = "/mongo")
    public ResponseEntity<Client> addNewClientInMongoDB(@Valid @RequestBody Client client)
    {
        Profile profile = new Profile(client.getProfile().getFirstName(), client.getProfile().getLastName(),
                client.getProfile().getGender());
        Client clientMongoDB = new Client(client.getAccountId(), profile);
        clientMongoDB = _clientMongoRepository.save(clientMongoDB);
        return new ResponseEntity<>(clientMongoDB, HttpStatus.OK);
    }

    @PostMapping(path = "/mysql")
    public ResponseEntity<ClientEntity> addNewClientInMysql(@Valid @RequestBody ClientEntity client)
    {
        ClientEntity clientEntity = new ClientEntity(client.getAccountId());
        ProfileEntity profile = new ProfileEntity(clientEntity, client.getProfile().getFirstName(),
                client.getProfile().getLastName(), client.getProfile().getGender());
        clientEntity.setProfile(profile);
        clientEntity.getProfile().setWebsite(client.getProfile().getWebsite());
        clientEntity.getProfile().setAddress(client.getProfile().getAddress());
        clientEntity.getProfile().setEmailAddress(client.getProfile().getEmailAddress());
        clientEntity.getProfile().setBirthday(client.getProfile().getBirthday());
        clientEntity = _clientJpaRepository.save(clientEntity);
        return new ResponseEntity<>(clientEntity, HttpStatus.OK);
    }


    //----------Update a Seller-----------------
    @PutMapping(path = "/mongo")
    public ResponseEntity<String> updateClientInMongoDB(@Valid @RequestBody Client client)
    {
        try
        {
            Client clientInDatabase = _clientMongoRepository.findById(client.getId()).orElseThrow(EntityNotFoundException::new);
            Update update = new Update();
            update.set("accountId", client.getAccountId());
            update.set("profile.firstName", client.getProfile().getFirstName());
            update.set("profile.lastName", client.getProfile().getLastName());
            update.set("profile.website", client.getProfile().getWebsite());
            update.set("profile.birthday", client.getProfile().getBirthday());
            update.set("profile.address", client.getProfile().getAddress());
            update.set("profile.emailAddress", client.getProfile().getEmailAddress());
            update.set("profile.gender", client.getProfile().getGender());

            Query query = new Query(Criteria.where("_id").is(client.getId()));
            UpdateResult updateResult = _mongoOperation.updateFirst(query, update, Client.class);
            if (updateResult.getModifiedCount() == 1)
            {
                clientInDatabase = _clientMongoRepository.findById(client.getId()).orElseThrow(EntityNotFoundException::new);
                System.out.println("__________________________________________________________________");
                System.out.println("The document of " + clientInDatabase.toString() + " updated");
                return new ResponseEntity<>("The seller updated", HttpStatus.OK);
            }
            else
            {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        catch (EntityNotFoundException e)
        {
            return new ResponseEntity<>("This seller doesn't exists in MongoDB.", HttpStatus.NOT_FOUND);
        }

    }

    @PutMapping(path = "/mysql")
    public ResponseEntity<String> updateClientInMysql(@Valid @RequestBody ClientEntity client)
    {
        ClientEntity sellerEntity = _clientJpaRepository.findById(client.getId()).orElse(null);
        if (sellerEntity == null)
        {
            return new ResponseEntity<>("This client doesn't exists in MySQL.", HttpStatus.NOT_FOUND);
        }
        sellerEntity.setAccountId(client.getAccountId());
        sellerEntity.getProfile().setFirstName(client.getProfile().getFirstName());
        sellerEntity.getProfile().setLastName(client.getProfile().getLastName());
        sellerEntity.getProfile().setWebsite(client.getProfile().getWebsite());
        sellerEntity.getProfile().setBirthday(client.getProfile().getBirthday());
        sellerEntity.getProfile().setAddress(client.getProfile().getAddress());
        sellerEntity.getProfile().setEmailAddress(client.getProfile().getEmailAddress());
        sellerEntity.getProfile().setGender(client.getProfile().getGender());
        sellerEntity = _clientJpaRepository.save(sellerEntity);
        System.out.println("__________________________________________________________________");
        System.out.println("The row of " + sellerEntity.toString() + " updated");
        return new ResponseEntity<>("The seller updated", HttpStatus.OK);
    }
}
