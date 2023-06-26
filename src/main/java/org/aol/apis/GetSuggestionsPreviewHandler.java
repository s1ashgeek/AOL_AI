package org.aol.apis;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import lombok.SneakyThrows;
import org.aol.dal.PromptDBDAO;
import org.aol.dependencies.DynamoDBFactory;
import org.aol.dependencies.ImageSuggestionFetcher;
import org.aol.models.*;
import org.aol.models.enums.PostContext;
import org.aol.models.enums.PostStatus;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GetSuggestionsPreviewHandler implements RequestHandler<GetSuggestionsPreviewRequest, SuggestionsPreviewResponse> {

    public ImageSuggestionFetcher imageSuggestionFetcher = new ImageSuggestionFetcher();

    @SneakyThrows
    @Override
    public SuggestionsPreviewResponse handleRequest(GetSuggestionsPreviewRequest input, Context context) {
        Post originalPost = Post.builder().imageLink(input.getImageS3URL())
                .textSuggestion(input.getText())
                .postStatus(PostStatus.ORIGINAL)
                .postMetadata(PostMetadata.builder().postContext(input.getPostContext()).Timestamp(DateTime.now()).build())
                .build();

        LambdaLogger logger = context.getLogger();
        logger.log("Input context: " + input.getPostContext());

        PromptDBDAO promptDBDAO = new PromptDBDAO(DynamoDBFactory.AmazonDynamoDBClient());
        String promptText = promptDBDAO.getPrompt(input.getPostContext().toString());
        logger.log("Prompt text: " + promptText + "\n");

        // Invoke ChatGPT API call here

        //Fetch images
        if (input.getImageS3URL() == null || input.getImageS3URL().isEmpty()) {
            String[] imageURLs = imageSuggestionFetcher.fetchImageURLs("cultural celebrations");
            Arrays.stream(imageURLs).forEach(imageURL -> logger.log("ImageURL: " + imageURL + "\n"));
        }

        // Combine output and create Generated Post object
        // (images, texts).forearch(postList)
        //

//        GeneratedPosts generatedPosts = GeneratedPosts.builder()
//                .PostId_UserId("123_456")
//                .PostContext_Timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")))
//                .GeneratedPosts(postList)
//                .UserInput(input)
//                .build();
        //)


        // Save Generated Post to Dynamo DB here

        // Return the Generated Post to the client here
        List<Post> postList = new ArrayList<>();
        postList.add(originalPost); // Replace with generated posts

        SuggestionsPreviewResponse suggestionsPreviewResponse = SuggestionsPreviewResponse.builder().posts(postList)
                .build();

        return suggestionsPreviewResponse;
    }
}
