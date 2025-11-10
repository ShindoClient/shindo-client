package me.miki.shindo.libs.openauth.microsoft.model.response;

public class MicrosoftDeviceCodePollResponse {
    private final MicrosoftRefreshResponse tokens;
    private final MicrosoftDeviceCodeErrorResponse error;

    private MicrosoftDeviceCodePollResponse(MicrosoftRefreshResponse tokens, MicrosoftDeviceCodeErrorResponse error) {
        this.tokens = tokens;
        this.error = error;
    }

    public static MicrosoftDeviceCodePollResponse success(MicrosoftRefreshResponse tokens) {
        return new MicrosoftDeviceCodePollResponse(tokens, null);
    }

    public static MicrosoftDeviceCodePollResponse failure(MicrosoftDeviceCodeErrorResponse error) {
        return new MicrosoftDeviceCodePollResponse(null, error);
    }

    public boolean isSuccess() {
        return tokens != null;
    }

    public MicrosoftRefreshResponse getTokens() {
        return tokens;
    }

    public MicrosoftDeviceCodeErrorResponse getError() {
        return error;
    }
}
