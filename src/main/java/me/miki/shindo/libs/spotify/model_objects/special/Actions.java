package me.miki.shindo.libs.spotify.model_objects.special;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.JsonObject;
import me.miki.shindo.libs.spotify.model_objects.AbstractModelObject;
import me.miki.shindo.libs.spotify.model_objects.specification.Disallows;
@JsonDeserialize(builder = Actions.Builder.class)
public class Actions extends AbstractModelObject {
    private final Disallows disallows;

    public Actions(Builder builder) {
        super(builder);
        this.disallows = builder.disallows;
    }
    public Disallows getDisallows() {
        return disallows;
    }

    @Override
    public String toString() {
        return "Actions(disallows=" + disallows + ")";
    }

    @Override
    public Builder builder() {
        return new Builder();
    }
    public static final class Builder extends AbstractModelObject.Builder {
        private Disallows disallows;
        public Builder setDisallows(Disallows disallows) {
            this.disallows = disallows;
            return this;
        }

        @Override
        public Actions build() {
            return new Actions(this);
        }
    }
    public static final class JsonUtil extends AbstractModelObject.JsonUtil<Actions> {
        @Override
        public Actions createModelObject(JsonObject jsonObject) {
            if (jsonObject == null || jsonObject.isJsonNull()) {
                return null;
            }

            return new Builder()
                    .setDisallows(
                            hasAndNotNull(jsonObject, "disallows")
                                    ? new Disallows.JsonUtil().createModelObject(
                                    jsonObject.getAsJsonObject("disallows"))
                                    : null)
                    .build();
        }
    }

}
