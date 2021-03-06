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

import br.com.audora.shopping.mongodb.models.Category;
import br.com.audora.shopping.mongodb.models.Product;
import br.com.audora.shopping.mongodb.repositories.CategoryRepository;

@RestController
@RequestMapping(path = "/category")
public class CategoryController
{
    final MongoOperations mongoOperation = new MongoTemplate(new MongoClient(), "local");
    @Autowired
    private CategoryRepository _categoryMongoRepository;

    /**
     * Get categories by name
     *
     * @param name
     * @return
     */
    @GetMapping(path = "")
    public ResponseEntity<Category> getCategory(@RequestParam(value = "name") String name)
    {
        Category categoryMongo = _categoryMongoRepository.findByName(name);
        if (categoryMongo != null)
        {
            return new ResponseEntity<>(categoryMongo, HttpStatus.OK);
        }
        System.out.println("There isn't any Category in Mongodb database with name: " + name);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * Lists all categories
     * @return
     */
    @GetMapping(path = "/all")
    public List<Category> getAllCategories()
    {
        return _categoryMongoRepository.findAll();
    }

    /**
     * Add new category
     * @param category
     * @return
     */
    @PostMapping(path = "")
    public ResponseEntity<Category> addNewCategory(@Valid @RequestBody Category category)
    {
        if (category == null || category.getName() == null || category.getName().trim().isEmpty())
        {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Category createdCategory = _categoryMongoRepository.save(category);
        return new ResponseEntity<>(createdCategory, HttpStatus.OK);
    }

    /**
     * Updates a category
     *
     * @param category
     * @return
     */
    @PutMapping(path = "")
    public ResponseEntity<String> updateCategory(@Valid @RequestBody Category category)
    {
        if (category == null || category.getId() == null || category.getName() == null ||
                category.getName().trim().isEmpty())
        {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Category categoryInDatabase = _categoryMongoRepository.findById(category.getId()).orElse(null);
        if (categoryInDatabase == null)
        {
            return new ResponseEntity<>("This category doesn't exists in MongoDB.", HttpStatus.NOT_FOUND);
        }

        //Update the name of the category in MongoDB Database using mongoOperation.updateFirst
        Update updateCat = new Update();
        updateCat.set("name", category.getName());
        Query queryCat = new Query(Criteria.where("_id").is(category.getId()));
        UpdateResult updateResult = mongoOperation.updateFirst(queryCat, updateCat, Category.class);
        if (updateResult.getModifiedCount() == 1)
        {
            //After updating a category, all of the products which are in this category must be updated manually.
            Query query = new Query();
            query.addCriteria(Criteria.where("fallIntoCategories._id").is(categoryInDatabase.getId()));
            Update update = new Update().set("fallIntoCategories.$.name", category.getName());
            updateResult = mongoOperation.updateMulti(query, update, Product.class);
            return new ResponseEntity<>("The category updated", HttpStatus.OK);
        }
        else
        {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
