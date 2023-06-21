package org.aol;

import lombok.Data;

@Data
public class PromptCreationRequest {
    private String promptType;
    private String promptText;

    public PromptCreationRequest(String promptType, String promptText) {
        this.promptType = promptType;
        this.promptText = promptText;
    }

    public PromptCreationRequest() {
        super();
    }

    public String getPromptType() {
        return this.promptType;
    }

    public String getPromptText() {
        return this.promptText;
    }

    public void setPromptType(String promptType) {
        this.promptType = promptType;
    }

    public void setPromptText(String promptText) {
        this.promptText = promptText;
    }
}
