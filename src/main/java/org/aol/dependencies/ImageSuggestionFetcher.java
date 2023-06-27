package org.aol.dependencies;
import lombok.NoArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@NoArgsConstructor
public class ImageSuggestionFetcher {
  private static final String PEXELS_API_URL = "https://api.pexels.com/v1/search";
  private static final int NUM_PHOTOS_PER_PAGE = 1;
  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String API_KEY = "etGMtSD8K8TdDM4Kxun9o62vQUN1K2oeaOhsCrPDnRe0mZu3IbfwOoCD";

  public String[] fetchImageURLs(String context) throws IOException {
    URL url = new URL(PEXELS_API_URL + "?query=" + context + "&per_page=" + NUM_PHOTOS_PER_PAGE);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("GET");
    connection.setRequestProperty(AUTHORIZATION_HEADER, API_KEY);

    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
    StringBuilder response = new StringBuilder();
    String line;

    while ((line = reader.readLine()) != null) {
      response.append(line);
    }

    reader.close();
    connection.disconnect();


    JSONArray photosArray = new JSONObject(response.toString()).getJSONArray("photos");
    String[] imageURLs = new String[photosArray.length()];

    for (int i = 0; i < photosArray.length(); i++) {
      JSONObject photoObj = photosArray.getJSONObject(i);
      imageURLs[i] = photoObj.getString("url");
    }

    return imageURLs;
  }

  //  Pexels API response structure
  private static class PexelsResponse {
    private Photo[] photos;

    public Photo[] getPhotos() {
      return photos;
    }
  }

  // Class representing the photo object in the Pexels API response
  private static class Photo {
    private String url;

    public String getUrl() {
      return url;
    }
  }


}
