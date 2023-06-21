package org.aol;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import org.aol.dependencies.DynamoDBFactory;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

/**
 * Lambda function entry point. You can change to use other pojo type or implement
 * a different RequestHandler.
 *
 * @see <a href=https://docs.aws.amazon.com/lambda/latest/dg/java-handler.html>Lambda Java Handler</a> for more information
 */
public class App implements RequestHandler<PromptCreationRequest, String> {
    private DynamoDbClient ddbClient;
    private String DYNAMODB_TABLE_NAME = "PromptDB";

//    public App() {
//        // Initialize the SDK client outside of the handler method so that it can be reused for subsequent invocations.
//        // It is initialized when the class is loaded.
//
//        // Consider invoking a simple api here to pre-warm up the application, eg: dynamodb#listTables
//    }

    @Override
    public String handleRequest(PromptCreationRequest input, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("Input: " + input);
        ddbClient = DynamoDBFactory.AmazonDynamoDBClient();
        PutItemRequest request = PutItemRequest.builder()
                .tableName(DYNAMODB_TABLE_NAME)
                .item(DynamoDBFactory.getAttributesMapForPromptDB(input.getPromptType(), input.getPromptText()))
                .build();
        ddbClient.putItem(request);
        return "Saved prompt: " + input.getPromptText() + " of type: " + input.getPromptType() + " to DynamoDB!";
    }
}
