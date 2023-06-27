package org.aol.apis;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import lombok.SneakyThrows;
import org.aol.dal.PromptDBDAO;
import org.aol.dependencies.DynamoDBFactory;
import org.aol.dependencies.ImageSuggestionFetcher;
import org.aol.dependencies.TextSuggestionFetcher;
import org.aol.models.*;
import org.aol.models.enums.PostContext;
import org.aol.models.enums.PostStatus;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
        TextSuggestionFetcher textSuggestionFetcher = new TextSuggestionFetcher(logger,input.getCreativity());
        String OpenAISuggestion = textSuggestionFetcher.GetOpenAISuggestions(input.getText() +" " + promptText);

        //Fetch images
        String gptimageURL = input.getImageS3URL();
        if (input.getImageS3URL() == null || input.getImageS3URL().isEmpty()) {
            String[] imageURLs = imageSuggestionFetcher.fetchImageURLs("cultural celebrations");
            Arrays.stream(imageURLs).forEach(imageURL -> logger.log("ImageURL: " + imageURL + "\n"));
            gptimageURL = imageURLs.length >= 1? imageURLs[0] : input.getImageS3URL();
        }

        // Combine Image and post here.
        // Here we are considering only 1st image to be combined with text. If we want multiple suggestions
        // then we need to call Open AI API multiple times which can be done iteratively.
         Post gpt_suggestedpost = Post.builder().imageLink(gptimageURL)
                .textSuggestion(OpenAISuggestion)
                .postStatus(PostStatus.GPT_SUGGESTED)
                .postMetadata(PostMetadata.builder().postContext(PostContext.WCF).Timestamp(DateTime.now(DateTimeZone.UTC)).build())
                .build();
        List<Post> postList = new ArrayList<>();
        postList.add(gpt_suggestedpost);

        GeneratedPosts generatedPosts = GeneratedPosts.builder()
                .postId_UserId("123_456") // We need to collect User information like device Id to populate this. Need to make changes to GetSuggestionsPreviewRequest obj.
                .postContext_Timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")))
                .generatedPosts(postList)
                .userInput(originalPost)
                .build();
        // Save Generated Post to Dynamo DB here


        // Save Generated Post to Dynamo DB here

        // Return the Generated Post to the client here
        postList.add(originalPost);
        SuggestionsPreviewResponse suggestionsPreviewResponse = SuggestionsPreviewResponse.builder().posts(postList)
                .build();

        return suggestionsPreviewResponse;
    }
}
