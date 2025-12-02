package com.project.Search.Repository.Impl;

import com.project.Search.DTO.SearchRequestDTO;
import com.project.Search.Entity.Product;
import com.project.Search.Repository.SearchCustomRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.regex.Pattern;

@Repository
public class SearchCustomRepositoryImpl implements SearchCustomRepository {

    private final MongoTemplate mongoTemplate;

    public SearchCustomRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Page<Product> searchProducts(SearchRequestDTO searchRequestDTO, Pageable pageable) {
        Query query = new Query();


        if (searchRequestDTO.getQuery() != null && !searchRequestDTO.getQuery().trim().isEmpty()) {
            String searchQuery = searchRequestDTO.getQuery().trim();

            Pattern pattern = Pattern.compile(".*" + Pattern.quote(searchQuery) + ".*", Pattern.CASE_INSENSITIVE);
            
            Criteria queryCriteria = new Criteria().orOperator(
                Criteria.where("name").regex(pattern),
                Criteria.where("description").regex(pattern),
                Criteria.where("brand").regex(pattern),
                Criteria.where("tags").regex(pattern),
                Criteria.where("sku").regex(pattern)
            );
            query.addCriteria(queryCriteria);
        }


        if (searchRequestDTO.getCategoryId() != null && !searchRequestDTO.getCategoryId().trim().isEmpty()) {
            query.addCriteria(Criteria.where("categories.id").is(searchRequestDTO.getCategoryId()));
        }

        if (searchRequestDTO.getBrand() != null && !searchRequestDTO.getBrand().trim().isEmpty()) {
            Pattern brandPattern = Pattern.compile(".*" + Pattern.quote(searchRequestDTO.getBrand().trim()) + ".*", Pattern.CASE_INSENSITIVE);
            query.addCriteria(Criteria.where("brand").regex(brandPattern));
        }


        if (searchRequestDTO.getMinPrice() != null || searchRequestDTO.getMaxPrice() != null) {
            Criteria priceCriteria = Criteria.where("price.amount");
            if (searchRequestDTO.getMinPrice() != null && searchRequestDTO.getMaxPrice() != null) {
                priceCriteria.gte(searchRequestDTO.getMinPrice()).lte(searchRequestDTO.getMaxPrice());
            } else if (searchRequestDTO.getMinPrice() != null) {
                priceCriteria.gte(searchRequestDTO.getMinPrice());
            } else if (searchRequestDTO.getMaxPrice() != null) {
                priceCriteria.lte(searchRequestDTO.getMaxPrice());
            }
            query.addCriteria(priceCriteria);
        }


        query.with(pageable);


        long total = mongoTemplate.count(query, Product.class);


        List<Product> products = mongoTemplate.find(query, Product.class);

        return new PageImpl<>(products, pageable, total);
    }
}
