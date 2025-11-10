package me.miki.shindo.libs.openauth.microsoft.model.response;

public class MicrosoftDeviceCodeResponse {
    private final String user_code;
    private final String device_code;
    private final String verification_uri;
    private final String verification_uri_complete;
    private final long expires_in;
    private final long interval;
    private final String message;

    public MicrosoftDeviceCodeResponse(String user_code, String device_code, String verification_uri,
                                       String verification_uri_complete, long expires_in, long interval,
                                       String message) {
        this.user_code = user_code;
        this.device_code = device_code;
        this.verification_uri = verification_uri;
        this.verification_uri_complete = verification_uri_complete;
        this.expires_in = expires_in;
        this.interval = interval;
        this.message = message;
    }

    public String getUserCode() {
        return user_code;
    }

    public String getDeviceCode() {
        return device_code;
    }

    public String getVerificationUri() {
        return verification_uri;
    }

    public String getVerificationUriComplete() {
        return verification_uri_complete;
    }

    public long getExpiresIn() {
        return expires_in;
    }

    public long getInterval() {
        return interval;
    }

    public String getMessage() {
        return message;
    }
}
