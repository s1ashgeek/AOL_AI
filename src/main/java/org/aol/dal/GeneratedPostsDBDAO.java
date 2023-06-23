package org.aol.dal;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.aol.models.GeneratedPosts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Data
@AllArgsConstructor
public class GeneratedPostsDBDAO {
    private DynamoDbClient ddbClient;
    private static String TABLE_NAME = "GeneratedPostsDB";
    private static String PRIMARY_KEY_POST = "PostId_UserId";
    private static String SORT_KEY_CONTEXT_TIMESTAMP = "PostContext_Timestamp";
    private static String GENERATED_POSTS = "GeneratedPosts";
    private static String USER_INPUT = "UserInput";
    private static Logger logger = LoggerFactory.getLogger(GeneratedPostsDBDAO.class);

    private void putGeneratedPosts(GeneratedPosts generatedPosts) {
        Map<String, AttributeValue> item = new HashMap<>();
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

        try {
            item.put(PRIMARY_KEY_POST, AttributeValue.builder().s(generatedPosts.getPostId_UserId()).build());
            item.put(SORT_KEY_CONTEXT_TIMESTAMP, AttributeValue.builder().s(generatedPosts.getPostContext_Timestamp()).build());
            item.put(GENERATED_POSTS, AttributeValue.builder().s(ow.writeValueAsString(generatedPosts.getGeneratedPosts())).build());
            item.put(USER_INPUT, AttributeValue.builder().s(ow.writeValueAsString(generatedPosts.getUserInput())).build());

            PutItemRequest request = PutItemRequest.builder()
                    .tableName(TABLE_NAME)
                    .item(item)
                    .build();

            try {
                ddbClient.putItem(request);
                logger.info(TABLE_NAME +" was successfully updated");
            } catch (ResourceNotFoundException e) {
                logger.error("Error: The Amazon DynamoDB table \"%s\" can't be found.\n", TABLE_NAME);
            } catch (DynamoDbException e) {
                logger.error(e.getMessage());
            }

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
