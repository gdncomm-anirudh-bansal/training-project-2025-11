package com.project.Search.Repository;

import com.project.Search.Entity.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ProductRepository extends MongoRepository<Product,String>,SearchCustomRepository {




}
