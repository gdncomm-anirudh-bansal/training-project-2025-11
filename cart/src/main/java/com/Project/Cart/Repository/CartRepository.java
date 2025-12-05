package com.Project.Cart.Repository;

import com.Project.Cart.Entity.Cart;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CartRepository extends MongoRepository<Cart,Long> {
}
