package example.buildsrc.dynamodb;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import org.apache.commons.lang3.StringUtils;

/**
 * Loads test products into the localstack dynamodb "catalog.products" table.
 */
public class ProductTableInitializer {

    private final DynamoDBMapper mapper;

    /**
     * Creates an instance of {@link ProductTableInitializer}.
     *
     * The LocalStack Gradle plugin requires a constructor that accepts the dynamodb client
     * as a single argument.
     *
     * @param dynamoClient
     */
    public ProductTableInitializer(AmazonDynamoDB dynamoClient) {
        this.mapper = new DynamoDBMapper(dynamoClient);
    }

    /**
     * Initializes the table.
     *
     * The LocalStack Gradle plugin requires a single void method named "run" that does not
     * accept any arguments.
     */
    public void run() {
        for (int i = 0; i < 10; i++) {
            final Product product = new Product();
            product.setId(StringUtils.leftPad(Integer.toString(i), 5, "0"));
            product.setName(String.format("Widget-%s", i));
            product.setDescription(String.format("Description for Widget-%s", i));
            product.setActive(true);
            product.setCreatedAt(System.currentTimeMillis());

            mapper.save(product);
        }
    }

    /**
     * Product record to load into dynamo table.
     */
    @DynamoDBTable(tableName = "catalog.products")
    public static class Product {

        @DynamoDBHashKey
        private String id;

        private String name;
        private String description;
        private boolean active;
        private long createdAt;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        public long getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(long createdAt) {
            this.createdAt = createdAt;
        }
    }
}
