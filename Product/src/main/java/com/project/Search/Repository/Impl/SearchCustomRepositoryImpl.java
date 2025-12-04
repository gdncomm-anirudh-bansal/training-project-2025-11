package com.project.Search.Repository.Impl;

import com.project.Search.DTO.SearchRequestDTO;
import com.project.Search.Entity.Product;
import com.project.Search.Repository.SearchCustomRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.TextQuery;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


@Repository
@Slf4j
public class SearchCustomRepositoryImpl implements SearchCustomRepository {

    private final MongoTemplate mongoTemplate;

    public SearchCustomRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Page<Product> searchProducts(SearchRequestDTO searchRequestDTO, Pageable pageable) {
        Query query = new Query();



        if (searchRequestDTO.getQuery() != null && !searchRequestDTO.getQuery().trim().isEmpty()) {

            TextCriteria textCriteria = TextCriteria.forDefaultLanguage()
                    .matching(searchRequestDTO.getQuery().trim());
            query = TextQuery.queryText(textCriteria).sortByScore();
        }

        if (searchRequestDTO.getCategoryId() != null && !searchRequestDTO.getCategoryId().trim().isEmpty()) {

            query.addCriteria(Criteria.where("categories.id").is(searchRequestDTO.getCategoryId()));
        }

        if (searchRequestDTO.getBrand() != null && !searchRequestDTO.getBrand().trim().isEmpty()) {

            query.addCriteria(Criteria.where("brand").is(searchRequestDTO.getBrand().trim()));


            String brandPattern = "^" + Pattern.quote(searchRequestDTO.getBrand().trim());
            query.addCriteria(Criteria.where("brand").regex(brandPattern, "i"));
        }

        if (searchRequestDTO.getMinPrice() != null || searchRequestDTO.getMaxPrice() != null) {

            Criteria priceCriteria = Criteria.where("price.amount");
            if (searchRequestDTO.getMinPrice() != null && searchRequestDTO.getMaxPrice() != null) {
                priceCriteria.gte(searchRequestDTO.getMinPrice()).lte(searchRequestDTO.getMaxPrice());
            } else if (searchRequestDTO.getMinPrice() != null) {
                priceCriteria.gte(searchRequestDTO.getMinPrice());
            } else {
                priceCriteria.lte(searchRequestDTO.getMaxPrice());
            }
            query.addCriteria(priceCriteria);
        }


        query.with(pageable);


        long total = mongoTemplate.count(query, Product.class);


        List<Product> products = mongoTemplate.find(query, Product.class);

        Document explainResult = mongoTemplate.executeCommand(
                new Document("explain",
                        new Document("find", "product")
                                .append("filter", query.getQueryObject())
                )
        );

        log.info("Query plan: {}", explainResult.toJson());

        return new PageImpl<>(products, pageable, total);
    }
}
