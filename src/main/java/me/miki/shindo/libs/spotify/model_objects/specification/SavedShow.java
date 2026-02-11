package me.miki.shindo.libs.spotify.model_objects.specification;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.JsonObject;
import me.miki.shindo.libs.spotify.SpotifyApi;
import me.miki.shindo.libs.spotify.model_objects.AbstractModelObject;

import java.text.ParseException;
import java.util.Date;
import java.util.logging.Level;
@JsonDeserialize(builder = SavedShow.Builder.class)
public class SavedShow extends AbstractModelObject {
    private final Date addedAt;
    private final ShowSimplified show;

    private SavedShow(final Builder builder) {
        super(builder);
        this.addedAt = builder.addedAt;
        this.show = builder.show;
    }
    public Date getAddedAt() {
        return addedAt;
    }
    public ShowSimplified getShow() {
        return show;
    }

    @Override
    public String toString() {
        return "SavedShow(addedAt=" + addedAt + ", show=" + show + ")";
    }

    @Override
    public Builder builder() {
        return new Builder();
    }
    public static final class Builder extends AbstractModelObject.Builder {
        private Date addedAt;
        private ShowSimplified show;
        public Builder setAddedAt(Date addedAt) {
            this.addedAt = addedAt;
            return this;
        }
        public Builder setShow(ShowSimplified show) {
            this.show = show;
            return this;
        }

        @Override
        public SavedShow build() {
            return new SavedShow(this);
        }
    }
    public static final class JsonUtil extends AbstractModelObject.JsonUtil<SavedShow> {
        @Override
        public SavedShow createModelObject(JsonObject jsonObject) {
            if (jsonObject == null || jsonObject.isJsonNull()) {
                return null;
            }

            try {
                return new Builder()
                        .setAddedAt(
                                hasAndNotNull(jsonObject, "added_at")
                                        ? SpotifyApi.parseDefaultDate(jsonObject.get("added_at").getAsString())
                                        : null)
                        .setShow(
                                hasAndNotNull(jsonObject, "show")
                                        ? new ShowSimplified.JsonUtil().createModelObject(
                                        jsonObject.getAsJsonObject("show"))
                                        : null)
                        .build();
            } catch (ParseException e) {
                SpotifyApi.LOGGER.log(Level.SEVERE, e.getMessage());
                return null;
            }
        }
    }
}
