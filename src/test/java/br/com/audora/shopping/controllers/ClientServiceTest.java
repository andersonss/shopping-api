package br.com.audora.shopping.controllers;

import br.com.audora.shopping.enums.Gender;
import br.com.audora.shopping.mongodb.models.*;
import br.com.audora.shopping.mongodb.repositories.CategoryRepository;
import br.com.audora.shopping.mongodb.repositories.ClientRepository;
import br.com.audora.shopping.mongodb.repositories.ProductRepository;
import com.mongodb.MongoClient;
import com.mongodb.client.result.UpdateResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;
import java.util.stream.Collectors;

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

    @Autowired
    private ProductRepository productMongoRepository;

    @Autowired
    private CategoryRepository categoryMongoRepository;

    @Test
    public void shouldGetClientByFirstName() {
        Profile profile = new Profile("Billy", "Sandey", Gender.Male);
        Client createdClient = clientMongoRepository.save(new Client(profile));
        ResponseEntity<Client> response = restTemplate.getForEntity(rootUrl + this.port +
                        "/client?firstName=Billy", Client.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertThat(createdClient.getProfile().getFirstName()).isEqualTo(response.getBody().getProfile().getFirstName());
    }

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

    @Test
    public void shouldCreateShoppingCart() {
        MongoOperations mongoOperation = new MongoTemplate(new MongoClient(), "local");

        Profile profile = new Profile("Billy", "Sandey", Gender.Male);
        Client createdClient = clientMongoRepository.save(new Client(profile));

        // Create four different categories in MongoDB
        Category furnitureCategory = new Category("Furniture");
        Category handmadeCategory = new Category("Handmade");
        furnitureCategory = categoryMongoRepository.save(furnitureCategory);
        handmadeCategory = categoryMongoRepository.save(handmadeCategory);
        Category kitchenCategory = new Category("Kitchen");
        kitchenCategory = categoryMongoRepository.save(kitchenCategory);
        Category woodCategory = new Category();
        woodCategory.setName("Wood");
        woodCategory = categoryMongoRepository.save(woodCategory);

        // Create a product in two different categories
        EmbeddedCategory woodEmbedded = new EmbeddedCategory(woodCategory.getId(), woodCategory.getName());
        EmbeddedCategory handmadeEmbedded = new EmbeddedCategory(handmadeCategory.getId(), handmadeCategory.getName());

        HashSet<EmbeddedCategory> categoryList = new HashSet<>(Arrays.asList(woodEmbedded, handmadeEmbedded));

        // The product
        Product desk = new Product("A Wooden Desk", "Made with thick solid reclaimed wood, Easy to " +
                "Assemble", 499.99f, categoryList);
        desk = productMongoRepository.save(desk);

        Update update = new Update();
        update.addToSet("productsOfCategory", desk.getId());
        List<String> ids = desk.getFallIntoCategories().stream().map(EmbeddedCategory::getId).collect(Collectors.toList());
        Query myUpdateQuery = new Query();
        myUpdateQuery.addCriteria(Criteria.where("_id").in(ids));
        UpdateResult updateResult = mongoOperation.updateMulti(myUpdateQuery, update, Category.class);
        System.out.println("__________________________________________________________________");
        System.out.println("The count of categories which updated after saving the desk is:  "
                + String.valueOf(updateResult.getMatchedCount()));


        // Create a product in one category
        EmbeddedCategory furnitureEmbedded = new EmbeddedCategory(furnitureCategory.getId(), furnitureCategory.getName());
        categoryList = new HashSet<>(Arrays.asList(furnitureEmbedded));

        // The product
        Product diningChair = new Product("Antique Dining Chair",
                "This mid-century fashionable chair is quite comfortable and attractive.", 234.20f,
                categoryList);
        diningChair = productMongoRepository.save(diningChair);

        update = new Update();
        update.addToSet("productsOfCategory", diningChair.getId());
        ids = diningChair.getFallIntoCategories().stream().map(EmbeddedCategory::getId).collect(Collectors.toList());
        myUpdateQuery = new Query();
        myUpdateQuery.addCriteria(Criteria.where("_id").in(ids));
        updateResult = mongoOperation.updateMulti(myUpdateQuery, update, Category.class);
        System.out.println("__________________________________________________________________");
        System.out.println("The count of categories which updated after saving the dining chair is:  "
                + String.valueOf(updateResult.getMatchedCount()));


        // Create a product in three different categories
        EmbeddedCategory kitchenEmbedded = new EmbeddedCategory(kitchenCategory.getId(), kitchenCategory.getName());
        categoryList = new HashSet<>(Arrays.asList(handmadeEmbedded, woodEmbedded, kitchenEmbedded));

        // The product
        Product spoon = new Product("Bamboo Spoon", "This is more durable than traditional hardwood " +
                "spoon, safe to use any cookware.", 13.11f, categoryList);
        spoon = productMongoRepository.save(spoon);

        update = new Update();
        update.addToSet("productsOfCategory", spoon.getId());
        ids = spoon.getFallIntoCategories().stream().map(EmbeddedCategory::getId).collect(Collectors.toList());
        myUpdateQuery = new Query();
        myUpdateQuery.addCriteria(Criteria.where("_id").in(ids));
        updateResult = mongoOperation.updateMulti(myUpdateQuery, update, Category.class);
        System.out.println("__________________________________________________________________");
        System.out.println("The count of categories which updated after saving wooden spoon is:  "
                + String.valueOf(updateResult.getMatchedCount()));

        // Create a shopping cart
        System.out.println("__________________________________________________________________");
        System.out.println("Adding shopping cart to client... ");
        ShoppingCart shoppingCart = new ShoppingCart();

        // Adding discount to the product
        desk.setDiscountPercentage(15);

        shoppingCart.addOrderProduct(desk);
        shoppingCart.addOrderProduct(spoon);
        shoppingCart.addOrderProduct(diningChair);

        HttpEntity<ShoppingCart> request = new HttpEntity<>(shoppingCart);
        ResponseEntity<ShoppingCart> response = restTemplate.getForEntity(rootUrl + this.port +
                "/client/shoppingcart?clientId=" + createdClient.getId(), ShoppingCart.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}

