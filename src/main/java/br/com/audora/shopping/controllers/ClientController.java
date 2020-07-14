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

import br.com.audora.shopping.mongodb.models.Profile;
import br.com.audora.shopping.mongodb.models.Client;
import br.com.audora.shopping.mongodb.repositories.ClientRepository;

@RestController
@RequestMapping(path = "/client")
public class ClientController
{
    private MongoOperations mongoOperation = new MongoTemplate(new MongoClient(), "local");
    @Autowired
    private ClientRepository clientMongoRepository;


    //----------Retrieve Clients----------------
    @GetMapping(path = "")
    public ResponseEntity<?> getClientsFromMongoDB(@RequestParam(value = "firstName") String firstName)
    {
        List<Client> clients = clientMongoRepository.findByFirstName(firstName);
        if (clients.size() > 0)
        {
            System.out.println("There are " + clients.size() + " sellers with first name " + firstName +
                    " in MongoDB database.");
            return new ResponseEntity<>(clients, HttpStatus.OK);
        }
        return new ResponseEntity<>("There isn't any seller with this name in MongoDB.", HttpStatus.NOT_FOUND);
    }

    @GetMapping(path = "/all")
    public List<Client> getAllClientsFromMongoDB()
    {
        return clientMongoRepository.findAll();
    }

    //----------Create a Client-----------------
    @PostMapping(path = "")
    public ResponseEntity<Client> addNewClientInMongoDB(@Valid @RequestBody Client client)
    {
        Profile profile = new Profile(client.getProfile().getFirstName(), client.getProfile().getLastName(),
                client.getProfile().getGender());
        Client clientMongoDB = new Client(profile);
        clientMongoDB = clientMongoRepository.save(clientMongoDB);
        return new ResponseEntity<>(clientMongoDB, HttpStatus.OK);
    }

    //----------Update a Seller-----------------
    @PutMapping(path = "")
    public ResponseEntity<String> updateClientInMongoDB(@Valid @RequestBody Client client)
    {
        try
        {
            Client clientInDatabase = clientMongoRepository.findById(client.getId()).orElseThrow(EntityNotFoundException::new);
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
            UpdateResult updateResult = mongoOperation.updateFirst(query, update, Client.class);
            if (updateResult.getModifiedCount() == 1)
            {
                clientInDatabase = clientMongoRepository.findById(client.getId()).orElseThrow(EntityNotFoundException::new);
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
}