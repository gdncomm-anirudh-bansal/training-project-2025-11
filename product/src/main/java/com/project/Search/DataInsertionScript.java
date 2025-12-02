//package com.project.Search;
//
//import com.project.Search.Entity.*;
//import com.project.Search.Repository.CategoryRepository;
//import com.project.Search.Repository.ProductRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.context.annotation.Bean;
//import org.springframework.data.mongodb.core.MongoTemplate;
//
//import java.util.*;
//import java.util.concurrent.ThreadLocalRandom;
//import java.util.stream.Collectors;
//
//@SpringBootApplication
//public class DataInsertionScript {
//
//    @Autowired
//    private CategoryRepository categoryRepository;
//
//    @Autowired
//    private ProductRepository productRepository;
//
//    @Autowired
//    private MongoTemplate mongoTemplate;
//
//    private static final String[] BRANDS = {
//        "Apple", "Samsung", "Sony", "LG", "Nike", "Adidas", "Dell", "HP", "Lenovo", "Canon",
//        "Nikon", "Bose", "JBL", "Microsoft", "Google", "Amazon", "Xiaomi", "OnePlus", "Huawei", "Motorola"
//    };
//
//    private static final String[] CATEGORY_NAMES = {
//        "Electronics", "Clothing", "Home & Kitchen", "Sports & Outdoors", "Books",
//        "Toys & Games", "Beauty & Personal Care", "Automotive", "Health & Fitness", "Garden & Tools"
//    };
//
//    private static final String[] PRODUCT_NAMES = {
//        "Smartphone", "Laptop", "Headphones", "Watch", "Tablet", "Camera", "Speaker", "Monitor",
//        "Keyboard", "Mouse", "T-Shirt", "Jeans", "Shoes", "Jacket", "Hat", "Backpack", "Wallet",
//        "Sunglasses", "Belt", "Socks", "Coffee Maker", "Blender", "Microwave", "Refrigerator",
//        "Washing Machine", "Vacuum Cleaner", "Iron", "Fan", "Lamp", "Chair", "Table", "Sofa",
//        "Bed", "Pillow", "Blanket", "Curtain", "Rug", "Mirror", "Clock", "Vase"
//    };
//
//    private static final String[] COLORS = {
//        "Black", "White", "Red", "Blue", "Green", "Yellow", "Silver", "Gold", "Gray", "Brown"
//    };
//
//    private static final String[] SIZES = {
//        "Small", "Medium", "Large", "XL", "XXL"
//    };
//
//    private static final String[] MATERIALS = {
//        "Cotton", "Polyester", "Leather", "Metal", "Plastic", "Wood", "Glass", "Ceramic", "Silk", "Wool"
//    };
//
//    public static void main(String[] args) {
//        SpringApplication.run(DataInsertionScript.class, args);
//    }
//
//    @Bean
//    CommandLineRunner run() {
//        return args -> {
//            System.out.println("Starting data insertion...");
//
//            // Clear existing data
//            mongoTemplate.getDb().getCollection("categories").drop();
//            mongoTemplate.getDb().getCollection("product").drop();
//
//            // Insert Categories
//            List<Category> categories = createCategories();
//            categoryRepository.saveAll(categories);
//            System.out.println("Inserted " + categories.size() + " categories");
//
//            // Insert Products
//            List<Product> products = createProducts(categories);
//            // Insert in batches to avoid memory issues
//            int batchSize = 1000;
//            for (int i = 0; i < products.size(); i += batchSize) {
//                int end = Math.min(i + batchSize, products.size());
//                List<Product> batch = products.subList(i, end);
//                productRepository.saveAll(batch);
//                System.out.println("Inserted products " + (i + 1) + " to " + end + " of " + products.size());
//            }
//
//            System.out.println("Data insertion completed successfully!");
//            System.out.println("Total categories: " + categories.size());
//            System.out.println("Total products: " + products.size());
//        };
//    }
//
//    private List<Category> createCategories() {
//        List<Category> categories = new ArrayList<>();
//        Random random = new Random();
//
//        // Create root categories
//        List<Category> rootCategories = new ArrayList<>();
//        for (int i = 0; i < CATEGORY_NAMES.length; i++) {
//            Category cat = new Category();
//            cat.setCategoryId("CAT-" + String.format("%03d", i + 1));
//            cat.setName(CATEGORY_NAMES[i]);
//            cat.setDescription("Category for " + CATEGORY_NAMES[i]);
//            cat.setParentId(null);
//            cat.setLevel(1);
//            cat.setPath("/" + CATEGORY_NAMES[i]);
//            cat.setImage("https://example.com/images/category/" + (i + 1) + ".jpg");
//            cat.setOrder(i + 1);
//            cat.setIsActive(true);
//            rootCategories.add(cat);
//        }
//        categories.addAll(rootCategories);
//
//        // Create subcategories (2-3 per root category)
//        int subCatId = CATEGORY_NAMES.length + 1;
//        for (Category rootCat : rootCategories) {
//            int numSubCats = 2 + random.nextInt(2); // 2-3 subcategories
//            for (int j = 0; j < numSubCats; j++) {
//                Category subCat = new Category();
//                subCat.setCategoryId("CAT-" + String.format("%03d", subCatId++));
//                subCat.setName(rootCat.getName() + " - Subcategory " + (j + 1));
//                subCat.setDescription("Subcategory of " + rootCat.getName());
//                subCat.setParentId(rootCat.getCategoryId());
//                subCat.setLevel(2);
//                subCat.setPath(rootCat.getPath() + "/Subcategory" + (j + 1));
//                subCat.setImage("https://example.com/images/category/" + subCatId + ".jpg");
//                subCat.setOrder(j + 1);
//                subCat.setIsActive(true);
//                categories.add(subCat);
//            }
//        }
//
//        return categories;
//    }
//
//    private List<Product> createProducts(List<Category> categories) {
//        List<Product> products = new ArrayList<>();
//        Random random = new Random();
//        ThreadLocalRandom threadRandom = ThreadLocalRandom.current();
//
//        // Filter active categories
//        List<Category> activeCategories = categories.stream()
//            .filter(c -> c.getIsActive() != null && c.getIsActive())
//            .collect(Collectors.toList());
//
//        for (int i = 1; i <= 50000; i++) {
//            Product product = new Product();
//
//            // Basic info
//            product.setSku("SKU-" + String.format("%08d", i));
//            product.setName(PRODUCT_NAMES[random.nextInt(PRODUCT_NAMES.length)] + " " +
//                           COLORS[random.nextInt(COLORS.length)] + " " + i);
//            product.setDescription("High quality " + product.getName() +
//                                 " with excellent features and durability. Perfect for everyday use.");
//            product.setBrand(BRANDS[random.nextInt(BRANDS.length)]);
//
//            // Categories (1-3 categories per product)
//            int numCategories = 1 + random.nextInt(3);
//            List<CategoryMapping> categoryMappings = new ArrayList<>();
//            Set<String> usedCategoryIds = new HashSet<>();
//            for (int j = 0; j < numCategories; j++) {
//                Category selectedCat = activeCategories.get(random.nextInt(activeCategories.size()));
//                if (!usedCategoryIds.contains(selectedCat.getCategoryId())) {
//                    CategoryMapping mapping = new CategoryMapping();
//                    mapping.setId(selectedCat.getCategoryId());
//                    mapping.setName(selectedCat.getName());
//                    mapping.setLevel(selectedCat.getLevel());
//                    categoryMappings.add(mapping);
//                    usedCategoryIds.add(selectedCat.getCategoryId());
//                }
//            }
//            product.setCategories(categoryMappings);
//
//            // Price
//            Price price = new Price();
//            long baseAmount = 1000 + threadRandom.nextLong(99000); // 1000 to 100000
//            price.setAmount(baseAmount);
//            price.setCurrency("USD");
//
//            // Discount (30% chance)
//            if (random.nextDouble() < 0.3) {
//                Discount discount = new Discount();
//                discount.setType("PERCENTAGE");
//                discount.setValue(10L + random.nextInt(40)); // 10-50% discount
//                Calendar cal = Calendar.getInstance();
//                discount.setStartDate(cal.getTime());
//                cal.add(Calendar.DAY_OF_MONTH, 30);
//                discount.setEndDate(cal.getTime());
//                price.setDiscount(discount);
//                price.setDiscountedAmount(baseAmount - (baseAmount * discount.getValue() / 100));
//            } else {
//                price.setDiscountedAmount(baseAmount);
//            }
//            product.setPrice(price);
//
//            // Images (1-4 images)
//            int numImages = 1 + random.nextInt(4);
//            List<Image> images = new ArrayList<>();
//            for (int j = 0; j < numImages; j++) {
//                Image image = new Image();
//                image.setUrl("https://example.com/images/products/" + i + "_" + j + ".jpg");
//                image.setAlt(product.getName() + " image " + (j + 1));
//                image.setIsPrimary(j == 0);
//                image.setOrder(j);
//                images.add(image);
//            }
//            product.setImages(images);
//
//            // Attributes
//            Map<String, Object> attributes = new HashMap<>();
//            attributes.put("color", COLORS[random.nextInt(COLORS.length)]);
//            attributes.put("size", SIZES[random.nextInt(SIZES.length)]);
//            attributes.put("material", MATERIALS[random.nextInt(MATERIALS.length)]);
//            attributes.put("weight", String.format("%.2f", 0.5 + random.nextDouble() * 10) + " kg");
//            attributes.put("dimensions", (10 + random.nextInt(50)) + "x" + (10 + random.nextInt(50)) + "x" + (5 + random.nextInt(20)) + " cm");
//            product.setAttributes(attributes);
//
//            // Specifications (3-6 specifications)
//            int numSpecs = 3 + random.nextInt(4);
//            List<Specification> specifications = new ArrayList<>();
//            String[] specNames = {"Warranty", "Model", "Year", "Country of Origin", "Warranty Period", "Color", "Size"};
//            String[] specValues = {"1 Year", "2024", "2024", "USA", "12 Months", "Various", "Various"};
//            Set<String> usedSpecs = new HashSet<>();
//            for (int j = 0; j < numSpecs; j++) {
//                int specIdx = random.nextInt(specNames.length);
//                if (!usedSpecs.contains(specNames[specIdx])) {
//                    Specification spec = new Specification();
//                    spec.setName(specNames[specIdx]);
//                    spec.setValue(specValues[specIdx]);
//                    specifications.add(spec);
//                    usedSpecs.add(specNames[specIdx]);
//                }
//            }
//            product.setSpecifications(specifications);
//
//            // Tags (2-5 tags)
//            int numTags = 2 + random.nextInt(4);
//            List<String> tags = new ArrayList<>();
//            String[] tagOptions = {"popular", "new", "bestseller", "sale", "premium", "eco-friendly", "durable", "lightweight"};
//            Set<String> usedTags = new HashSet<>();
//            for (int j = 0; j < numTags; j++) {
//                String tag = tagOptions[random.nextInt(tagOptions.length)];
//                if (!usedTags.contains(tag)) {
//                    tags.add(tag);
//                    usedTags.add(tag);
//                }
//            }
//            product.setTags(tags);
//
//            // Stock
//            Stock stock = new Stock();
//            if (random.nextDouble() < 0.1) {
//                stock.setUnlimited(true);
//                stock.setQuantity(null);
//            } else {
//                stock.setUnlimited(false);
//                stock.setQuantity(10 + random.nextInt(990)); // 10-1000
//            }
//            stock.setStatus(stock.getQuantity() != null && stock.getQuantity() > 0 ? "IN_STOCK" : "OUT_OF_STOCK");
//            product.setStock(stock);
//
//            // Rating
//            Rating rating = new Rating();
//            rating.setAverage(3.0 + random.nextDouble() * 2.0); // 3.0 to 5.0
//            rating.setCount(10 + random.nextInt(990)); // 10-1000 reviews
//            product.setRatings(rating);
//
//            // Status
//            String[] statuses = {"ACTIVE", "INACTIVE", "DRAFT"};
//            product.setStatus(statuses[random.nextInt(statuses.length)]);
//
//            products.add(product);
//
//            // Progress indicator
//            if (i % 5000 == 0) {
//                System.out.println("Generated " + i + " products...");
//            }
//        }
//
//        return products;
//    }
//}
//
