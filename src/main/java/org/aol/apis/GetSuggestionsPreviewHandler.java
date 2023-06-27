package org.aol.apis;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import lombok.SneakyThrows;
import org.aol.dal.GeneratedPostsDBDAO;
import org.aol.dal.PromptDBDAO;
import org.aol.dependencies.DynamoDBFactory;
import org.aol.dependencies.ImageSuggestionFetcher;
import org.aol.dependencies.TextSuggestionFetcher;
import org.aol.models.*;
import org.aol.models.enums.PostContext;
import org.aol.models.enums.PostStatus;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static java.util.stream.Collectors.toList;

public class GetSuggestionsPreviewHandler implements RequestHandler<GetSuggestionsPreviewRequest, SuggestionsPreviewResponse> {

    public ImageSuggestionFetcher imageSuggestionFetcher = new ImageSuggestionFetcher();
    private static String IMAGE_SEARCH_QUERY = "cultural celebrations";

    @SneakyThrows
    @Override
    public SuggestionsPreviewResponse handleRequest(GetSuggestionsPreviewRequest input, Context context) {
        Post originalPost = generatePost(input.getImageS3URL(), input.getText(), PostStatus.ORIGINAL, input.getPostContext());

        LambdaLogger logger = context.getLogger();
        logger.log("Input context: " + input.getPostContext() + "\n");

        String promptText = getPromptText(input);
        logger.log("Prompt text: " + promptText + "\n");

        List<String> imageAndTextSuggestions = getImageAndTextSuggestions(input, logger, promptText);
        String suggestedImageURL = imageAndTextSuggestions.get(0);
        logger.log("Image Suggestion: " + suggestedImageURL + "\n");
        String textSuggestion = imageAndTextSuggestions.get(1);
        logger.log("Text Suggestion: " + textSuggestion + "\n");

        List<Post> suggestedAndOriginalPosts = new ArrayList<>();
        Post suggestedPost = generatePost(suggestedImageURL, textSuggestion, PostStatus.GPT_SUGGESTED, input.getPostContext());
        generateAndSaveSuggestedPostList(originalPost, Arrays.asList(suggestedPost), input);

        suggestedAndOriginalPosts.add(suggestedPost);
        suggestedAndOriginalPosts.add(originalPost);
        SuggestionsPreviewResponse suggestionsPreviewResponse = SuggestionsPreviewResponse.builder().posts(suggestedAndOriginalPosts).build();

        return suggestionsPreviewResponse;
    }

    private void generateAndSaveSuggestedPostList(Post originalPost, List<Post> suggestedPosts, GetSuggestionsPreviewRequest input) {
        GeneratedPosts generatedPosts = GeneratedPosts.builder()
                .postId_UserId(UUID.randomUUID() + "_" + input.getUserMetadata().getUserId())
                .postContext_Timestamp(input.getPostContext() + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")))
                .generatedPosts(suggestedPosts)
                .userInput(originalPost)
                .build();

        GeneratedPostsDBDAO generatedPostsDBDAO = new GeneratedPostsDBDAO(DynamoDBFactory.AmazonDynamoDBClient());
        generatedPostsDBDAO.saveGeneratedPosts(generatedPosts);
    }

    private List<String> getImageAndTextSuggestions(GetSuggestionsPreviewRequest input, LambdaLogger logger, String promptText) {
        Executor executor = Executors.newFixedThreadPool(2);

        List<CompletableFuture<String>> futures = new ArrayList<>();
        futures.add(CompletableFuture.supplyAsync(() -> getImageSuggestion(input, logger), executor));
        futures.add(CompletableFuture.supplyAsync(() -> getTextSuggestion(input, logger, promptText), executor));
        List<String> imageAndTextSuggestion = futures.stream().map(CompletableFuture::join).collect(toList());
//        String textSuggestion = getTextSuggestion(input, logger, promptText);
//        String suggestedImageURL = getImageSuggestion(input, logger);
        return imageAndTextSuggestion;
    }

    private Post generatePost(String suggestedImageURL, String textSuggestion, PostStatus postStatus, PostContext postContext) {
        return Post.builder().imageLink(suggestedImageURL)
                .textSuggestion(textSuggestion)
                .postStatus(postStatus)
                .postMetadata(PostMetadata.builder().postContext(postContext).timestamp(Date.from(Instant.now())).build())
                .build();
    }

    private String getImageSuggestion(GetSuggestionsPreviewRequest input, LambdaLogger logger)  {
        String suggestedImageURL = input.getImageS3URL();
        if (input.getImageS3URL() == null || input.getImageS3URL().isEmpty()) {
            String[] imageURLs = new String[0];
            try {
                imageURLs = imageSuggestionFetcher.fetchImageURLs(IMAGE_SEARCH_QUERY);
                suggestedImageURL = imageURLs.length >= 1? imageURLs[0] : input.getImageS3URL();
            } catch (IOException e) {
                logger.log("Error fetching image URL: " + e.getMessage());
            }
        }
        return suggestedImageURL;
    }

    private String getTextSuggestion(GetSuggestionsPreviewRequest input, LambdaLogger logger, String promptText) {
        TextSuggestionFetcher textSuggestionFetcher = new TextSuggestionFetcher(logger, input.getCreativity());
        return textSuggestionFetcher.GetOpenAISuggestions(promptText + "\n[" + input.getText() + "]\n");
    }

    private String getPromptText(GetSuggestionsPreviewRequest input) {
        PromptDBDAO promptDBDAO = new PromptDBDAO(DynamoDBFactory.AmazonDynamoDBClient());
        return promptDBDAO.getPrompt(input.getPostContext().toString());
    }
}
