package me.miki.shindo.libs.spotify.model_objects.specification;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.JsonObject;
import me.miki.shindo.libs.spotify.enums.CopyrightType;
import me.miki.shindo.libs.spotify.model_objects.AbstractModelObject;
@JsonDeserialize(builder = Copyright.Builder.class)
public class Copyright extends AbstractModelObject {
    private final String text;
    private final CopyrightType type;

    private Copyright(final Builder builder) {
        super(builder);

        this.text = builder.text;
        this.type = builder.type;
    }
    public String getText() {
        return text;
    }
    public CopyrightType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Copyright(text=" + text + ", type=" + type + ")";
    }

    @Override
    public Builder builder() {
        return new Builder();
    }
    public static final class Builder extends AbstractModelObject.Builder {
        private String text;
        private CopyrightType type;
        public Builder setText(String text) {
            this.text = text;
            return this;
        }
        public Builder setType(CopyrightType type) {
            this.type = type;
            return this;
        }

        @Override
        public Copyright build() {
            return new Copyright(this);
        }
    }
    public static final class JsonUtil extends AbstractModelObject.JsonUtil<Copyright> {
        public Copyright createModelObject(JsonObject jsonObject) {
            if (jsonObject == null || jsonObject.isJsonNull()) {
                return null;
            }

            return new Copyright.Builder()
                    .setText(
                            hasAndNotNull(jsonObject, "text")
                                    ? jsonObject.get("text").getAsString()
                                    : null)
                    .setType(
                            hasAndNotNull(jsonObject, "type")
                                    ? CopyrightType.keyOf(
                                    jsonObject.get("type").getAsString().toLowerCase())
                                    : null)
                    .build();
        }
    }
}
