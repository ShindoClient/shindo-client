package me.miki.shindo.libs.spotify.requests.data.browse;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import me.miki.shindo.libs.i18n.CountryCode;
import me.miki.shindo.libs.i18n.LanguageCode;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.model_objects.specification.Category;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

@JsonDeserialize(builder = GetCategoryRequest.Builder.class)
public class GetCategoryRequest extends AbstractDataRequest<Category> {
    private GetCategoryRequest(final Builder builder) {
        super(builder);
    }

    public Category execute() throws
            IOException,
            SpotifyWebApiException,
            ParseException {
        return new Category.JsonUtil().createModelObject(getJson());
    }

    public static final class Builder extends AbstractDataRequest.Builder<Category, Builder> {
        public Builder(final String accessToken) {
            super(accessToken);
        }

        public Builder category_id(final String category_id) {
            assert (category_id != null);
            assert (category_id.matches("^[a-z]+$"));
            return setPathParameter("category_id", category_id);
        }

        public Builder country(final CountryCode country) {
            assert (country != null);
            return setQueryParameter("country", country);
        }

        public Builder locale(final String locale) {
            assert (locale != null);
            assert (locale.contains("_"));
            String[] localeParts = locale.split("_");
            assert (localeParts.length == 2);
            assert (LanguageCode.getByCode(localeParts[0]) != null);
            assert (CountryCode.getByCode(localeParts[1]) != null);
            return setQueryParameter("locale", locale);
        }

        @Override
        public GetCategoryRequest build() {
            setPath("/v1/browse/categories/{category_id}");
            return new GetCategoryRequest(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
