package me.miki.shindo.libs.spotify.model_objects.specification;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.JsonObject;
import me.miki.shindo.libs.spotify.model_objects.AbstractModelObject;
@JsonDeserialize(builder = Followers.Builder.class)
public class Followers extends AbstractModelObject {
    private final String href;
    private final Integer total;

    private Followers(final Builder builder) {
        super(builder);

        this.href = builder.href;
        this.total = builder.total;
    }
    public String getHref() {
        return href;
    }
    public Integer getTotal() {
        return total;
    }

    @Override
    public String toString() {
        return "Followers(href=" + href + ", total=" + total + ")";
    }

    @Override
    public Builder builder() {
        return new Builder();
    }
    public static final class Builder extends AbstractModelObject.Builder {
        private String href;
        private Integer total;
        public Builder setHref(String href) {
            this.href = href;
            return this;
        }
        public Builder setTotal(Integer total) {
            this.total = total;
            return this;
        }

        @Override
        public Followers build() {
            return new Followers(this);
        }
    }
    public static final class JsonUtil extends AbstractModelObject.JsonUtil<Followers> {
        public Followers createModelObject(JsonObject jsonObject) {
            if (jsonObject == null || jsonObject.isJsonNull()) {
                return null;
            }

            return new Followers.Builder()
                    .setHref(
                            hasAndNotNull(jsonObject, "href")
                                    ? jsonObject.get("href").getAsString()
                                    : null)
                    .setTotal(
                            hasAndNotNull(jsonObject, "total")
                                    ? jsonObject.get("total").getAsInt()
                                    : null)
                    .build();
        }
    }
}
