package org.aol.dependencies;

import java.io.IOException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
/*
This class will call the Open AI to get the generated text.
 */
public class TextSuggestionFetcher {

    // Move the below secret to key vault and fetch from key vault. Not sure of key vault equivalent in AWS.
    private static final String apiKey = "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";
    private static final String apiUrl = "https://api.openai.com/v1/engines/text-davinci-003/completions";
    private static double temperature = 0.83;
    private static final int maxTokens = 3671;
    private static final double frequencyPenalty = 0.92;
    private static final double presencePenalty = 1.04;
    private static final double topP = 1.0;
    private static final int bestOf = 4;

    private LambdaLogger logger;

    public TextSuggestionFetcher(LambdaLogger logger, double creativity) {
        this.logger = logger;
        temperature = creativity;
    }

    public String GetOpenAISuggestions(String prompt) {
        try {
            logger.log("starting GetOpenAISuggestions method");
            HttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(apiUrl);

            httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
            httpPost.setHeader("Authorization", "Bearer " + apiKey);

            logger.log("Prompt message sent to Open AI is " + prompt);

            StringEntity requestEntity = new StringEntity(
                    "{\"prompt\":\"" + prompt + "\",\"temperature\":" + temperature +
                            ",\"max_tokens\":" + maxTokens + ",\"frequency_penalty\":" + frequencyPenalty +
                            ",\"presence_penalty\":" + presencePenalty + ",\"top_p\":" + topP +
                            ",\"best_of\":" + bestOf + "}", "UTF-8");

            httpPost.setEntity(requestEntity);
            logger.log("Calling OPenAI APIs using HttpClient");
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity responseEntity = response.getEntity();
            logger.log("Got response from OPenAI APIs is " + responseEntity);
            if (responseEntity != null) {
                String responseBody = EntityUtils.toString(responseEntity);
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(responseBody);

                if (jsonNode.has("choices")) {
                    JsonNode choicesNode = jsonNode.get("choices");

                    if (choicesNode.isArray() && choicesNode.size() > 0) {
                        JsonNode completionNode = choicesNode.get(0);

                        if (completionNode.has("text")) {
                            logger.log("OpenAI Response has text");
                            return completionNode.get("text").asText();
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.log("There is some error in Open AI call and returning same response as user");
        return prompt;
    }
}
