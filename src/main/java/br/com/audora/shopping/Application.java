package br.com.audora.shopping;

import br.com.audora.shopping.mongodb.models.*;
import com.mongodb.MongoClient;
import com.mongodb.client.result.UpdateResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import br.com.audora.shopping.enums.Gender;
import br.com.audora.shopping.mongodb.repositories.CategoryRepository;
import br.com.audora.shopping.mongodb.repositories.ProductRepository;
import br.com.audora.shopping.mongodb.repositories.ClientRepository;

@EnableMongoRepositories(basePackages = "br.com.audora.shopping.mongodb.repositories")
@SpringBootApplication
public class Application implements CommandLineRunner
{
    @Autowired
    private CategoryRepository categoryMongoRepository;
    @Autowired
    private ProductRepository productMongoRepository;
    @Autowired
    private ClientRepository clientMongoRepository;

    public static void main(String[] args)
    {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... strings) throws Exception
    {
        MongoOperations mongoOperation = new MongoTemplate(new MongoClient(), "local");
        categoryMongoRepository.deleteAll();
        clientMongoRepository.deleteAll();
        productMongoRepository.deleteAll();

        //--------------Create a client-----------------------------------------------
        Profile profile = new Profile("Billy", "Sandey", Gender.Male);
        Client client = new Client(profile);
        clientMongoRepository.save(client);

        System.out.println("__________________________________________________________________");
        System.out.println("Test MongoDB repository");
        System.out.println("Find client(s) by first name");
        clientMongoRepository.findByFirstName("Anderson").forEach(System.out::println);
        System.out.println("__________________________________________________________________");


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
        System.out.println("Adding shopping cart to client:  ");
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.addOrderProduct(desk);
        shoppingCart.addOrderProduct(spoon);
        shoppingCart.addOrderProduct(diningChair);

        client.setShoppingCart(shoppingCart);

        clientMongoRepository.save(client);
    }
}
