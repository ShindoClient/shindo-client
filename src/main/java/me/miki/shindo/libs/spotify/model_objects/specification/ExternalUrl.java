package me.miki.shindo.libs.spotify.model_objects.specification;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.miki.shindo.libs.spotify.model_objects.AbstractModelObject;

import java.util.Map;
@JsonDeserialize(builder = ExternalUrl.Builder.class)
public class ExternalUrl extends AbstractModelObject {
    private final Map<String, String> externalUrls;

    private ExternalUrl(final Builder builder) {
        super(builder);

        this.externalUrls = builder.externalUrls;
    }
    public String get(String key) {
        return externalUrls.get(key);
    }
    public Map<String, String> getExternalUrls() {
        return externalUrls;
    }

    @Override
    public String toString() {
        return "ExternalUrl(externalUrls=" + externalUrls + ")";
    }

    @Override
    public Builder builder() {
        return new Builder();
    }
    public static final class Builder extends AbstractModelObject.Builder {
        private Map<String, String> externalUrls;
        public Builder setExternalUrls(Map<String, String> externalUrls) {
            this.externalUrls = externalUrls;
            return this;
        }

        @Override
        public ExternalUrl build() {
            return new ExternalUrl(this);
        }
    }
    @SuppressWarnings("unchecked")
    public static final class JsonUtil extends AbstractModelObject.JsonUtil<ExternalUrl> {
        public ExternalUrl createModelObject(JsonObject jsonObject) {
            if (jsonObject == null || jsonObject.isJsonNull()) {
                return null;
            }

            Map<String, String> map = new Gson().fromJson(jsonObject, Map.class);

            return new ExternalUrl.Builder()
                    .setExternalUrls(map)
                    .build();
        }
    }

}
