
package org.aol.dependencies;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import java.util.HashMap;
import java.util.Map;


public class DynamoDBFactory {
    private static Region REGION = Region.US_WEST_1;
    private DynamoDBFactory() {}
    public static DynamoDbClient AmazonDynamoDBClient() {
        return DynamoDbClient.builder().region(REGION).build();
    }

    public static Map<String, AttributeValue> getAttributesMapForPromptDB(String promptType, String promptText) {
        Map<String, AttributeValue> item = new HashMap<>();
        if (promptType == null || promptType.isEmpty() || promptText == null || promptText.isEmpty()) {
            promptType = "OverriddenText";
            promptText = "OverriddenText";
        }
        item.put("PromptType", AttributeValue.builder().s(promptType).build());
        item.put("PromptText", AttributeValue.builder().s(promptText).build());
        return item;
    }
}
