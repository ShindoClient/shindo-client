package me.miki.shindo.libs.openauth.microsoft.model.response;

public class MicrosoftDeviceCodeErrorResponse {
    private final String error;
    private final String error_description;

    public MicrosoftDeviceCodeErrorResponse(String error, String error_description) {
        this.error = error;
        this.error_description = error_description;
    }

    public String getError() {
        return error;
    }

    public String getErrorDescription() {
        return error_description;
    }
}
