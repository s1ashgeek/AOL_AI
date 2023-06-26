package org.aol.dal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
@Data
@AllArgsConstructor
public class PromptDBDAO {
    private DynamoDbClient ddbClient;
    private static String TABLE_NAME = "PromptDB";
    private static String PRIMARY_KEY = "PromptType";
    private static String PROMPT_TEXT = "PromptText";
    private static Logger logger = LoggerFactory.getLogger(PromptDBDAO.class);

    public String getPrompt(String promptKey) {
        HashMap<String,AttributeValue> keyToGet = new HashMap<>();
        keyToGet.put(PRIMARY_KEY, AttributeValue.builder().s(promptKey).build());

        GetItemRequest request = GetItemRequest.builder()
                .key(keyToGet)
                .tableName(TABLE_NAME)
                .build();

        String prompt = "";

        try {
            Map<String,AttributeValue> returnedItem = ddbClient.getItem(request).item();
            if (returnedItem != null) {
                AttributeValue promptTextValue = returnedItem.get(PROMPT_TEXT);
                prompt = promptTextValue.s();
            } else {
                logger.info("No item found with the key %s!\n", PRIMARY_KEY);
            }
        } catch (DynamoDbException e) {
            logger.error(e.getMessage());
        }

        return prompt;
    }
}
