package br.com.audora.shopping.controllers;

import br.com.audora.shopping.mongodb.repositories.ProductRepository;
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

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;

import br.com.audora.shopping.mongodb.models.Category;
import br.com.audora.shopping.mongodb.models.EmbeddedCategory;
import br.com.audora.shopping.mongodb.models.Product;
import br.com.audora.shopping.mongodb.models.Client;
import br.com.audora.shopping.mongodb.repositories.CategoryRepository;
import br.com.audora.shopping.mongodb.repositories.ClientRepository;

@RestController
@RequestMapping(path = "/product")
public class ProductController
{
    final MongoOperations mongoOperations = new MongoTemplate(new MongoClient(), "local");
    @Autowired
    private ProductRepository productMongoRepository;
    @Autowired
    private ClientRepository sellerMongoRepository;
    @Autowired
    private CategoryRepository categoryMongoRepository;

    /**
     * Retrieve Products
     *
     * @param name
     * @return
     */
    @GetMapping(path = "")
    public ResponseEntity<Product> getProduct(@RequestParam(value = "name") String name)
    {
        Product productMongo = productMongoRepository.findByName(name);
        if (productMongo != null)
        {
            return new ResponseEntity<>(productMongo, HttpStatus.OK);
        }
        System.out.println("There isn't any Product in Mongodb database with name: " + name);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * List all products.
     *
     * @return
     */
    @GetMapping(path = "/all")
    public List<Product> getAllProductsFromMongoDB()
    {
        return productMongoRepository.findAll();
    }

    /**
     * Creates a Product
     *
     * @param product
     * @return
     */
    @PostMapping(path = "")
    public ResponseEntity<?> addNewProduct(@Valid @RequestBody Product product)
    {
        Client seller;
        HashSet<EmbeddedCategory> categories = new HashSet<>();
        try
        {
            for (EmbeddedCategory embCat : product.getFallIntoCategories())
            {
                Category category = categoryMongoRepository.findById(embCat.getId())
                        .orElseThrow(EntityNotFoundException::new);
                categories.add(new EmbeddedCategory(category.getId(), category.getName()));
            }
        }
        catch (EntityNotFoundException e)
        {
            return new ResponseEntity<>("One of the categories which the product falls into, doesn't exists!",
                    HttpStatus.BAD_REQUEST);
        }
        if (categories.isEmpty())
        {
            return new ResponseEntity<>("The product must belongs to at least one category!",
                    HttpStatus.BAD_REQUEST);
        }
        Product productMongoDB = new Product(product.getName(), product.getDescription(), product.getPrice(),
                categories);
        productMongoDB = productMongoRepository.save(productMongoDB);
        //add a reference to this product in appropriate categories
        Update update = new Update();
        update.addToSet("productsOfCategory", productMongoDB.getId());
        List<String> catIds = productMongoDB.getFallIntoCategories().stream().map(EmbeddedCategory::getId)
                .collect(Collectors.toList());
        Query query = new Query().addCriteria(Criteria.where("_id").in(catIds));
        UpdateResult updateResult = mongoOperations.updateMulti(query, update, Category.class);
        System.out.println("The new product added and " + updateResult.getModifiedCount() + " categories updated.");
        return new ResponseEntity<>(productMongoDB, HttpStatus.OK);
    }

    /**
     * Updates a Product
     *
     * @param product
     * @return
     */
    @PutMapping(path = "")
    public ResponseEntity<String> updateProduct(@Valid @RequestBody Product product)
    {
        Product productInDatabase = productMongoRepository.findById(product.getId()).orElse(null);
        if (productInDatabase == null)
        {
            return new ResponseEntity<>("This product doesn't exists in MongoDB.", HttpStatus.NOT_FOUND);
        }
        HashSet<EmbeddedCategory> categories = new HashSet<>();
        try
        {
            for (EmbeddedCategory embCat : product.getFallIntoCategories())
            {
                Category category = categoryMongoRepository.findById(embCat.getId())
                        .orElseThrow(EntityNotFoundException::new);
                categories.add(new EmbeddedCategory(category.getId(), category.getName()));
            }
        }
        catch (EntityNotFoundException e)
        {
            return new ResponseEntity<>("One of the categories which the product falls into, doesn't exists!",
                    HttpStatus.BAD_REQUEST);
        }
        if (categories.isEmpty())
        {
            return new ResponseEntity<>("The product must belongs to at least one category!",
                    HttpStatus.BAD_REQUEST);
        }
        //Update the product by setting each property of this product in a update query.
        Update update = new Update();
        update.set("name", product.getName());
        update.set("description", product.getDescription());
        update.set("price", product.getPrice());
        update.set("image_URLs", product.getImage_URLs());
        update.set("fallIntoCategories", categories);
        Query query = new Query(Criteria.where("_id").is(product.getId()));
        UpdateResult updateResult = mongoOperations.updateFirst(query, update, Product.class);
        if (updateResult.getModifiedCount() == 1)
        {
            productInDatabase = productMongoRepository.findById(product.getId()).get();
            System.out.println("The \"" + productInDatabase.getName() + "\" product updated!");
            return new ResponseEntity<>("The product updated", HttpStatus.OK);
        }
        else
        {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
