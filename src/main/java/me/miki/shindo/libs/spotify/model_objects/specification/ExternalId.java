package me.miki.shindo.libs.spotify.model_objects.specification;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.miki.shindo.libs.spotify.model_objects.AbstractModelObject;

import java.util.Map;
@JsonDeserialize(builder = ExternalId.Builder.class)
public class ExternalId extends AbstractModelObject {
    private final Map<String, String> externalIds;

    private ExternalId(final Builder builder) {
        super(builder);

        this.externalIds = builder.externalIds;
    }
    public Map<String, String> getExternalIds() {
        return externalIds;
    }

    @Override
    public String toString() {
        return "ExternalId(externalIds=" + externalIds + ")";
    }

    @Override
    public Builder builder() {
        return new Builder();
    }
    public static final class Builder extends AbstractModelObject.Builder {
        private Map<String, String> externalIds;
        public Builder setExternalIds(Map<String, String> externalIds) {
            this.externalIds = externalIds;
            return this;
        }

        @Override
        public ExternalId build() {
            return new ExternalId(this);
        }
    }
    @SuppressWarnings("unchecked")
    public static final class JsonUtil extends AbstractModelObject.JsonUtil<ExternalId> {
        public ExternalId createModelObject(JsonObject jsonObject) {
            if (jsonObject == null || jsonObject.isJsonNull()) {
                return null;
            }

            Map<String, String> map = new Gson().fromJson(jsonObject, Map.class);

            return new ExternalId.Builder()
                    .setExternalIds(map)
                    .build();
        }
    }
}
