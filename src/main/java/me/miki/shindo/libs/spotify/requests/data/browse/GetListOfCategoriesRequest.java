package me.miki.shindo.libs.spotify.requests.data.browse;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import me.miki.shindo.libs.i18n.CountryCode;
import me.miki.shindo.libs.i18n.LanguageCode;
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException;
import me.miki.shindo.libs.spotify.model_objects.specification.Category;
import me.miki.shindo.libs.spotify.model_objects.specification.Paging;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataPagingRequest;
import me.miki.shindo.libs.spotify.requests.data.AbstractDataRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

@JsonDeserialize(builder = GetListOfCategoriesRequest.Builder.class)
public class GetListOfCategoriesRequest extends AbstractDataRequest<Paging<Category>> {
    private GetListOfCategoriesRequest(final Builder builder) {
        super(builder);
    }

    public Paging<Category> execute() throws
            IOException,
            SpotifyWebApiException,
            ParseException {
        return new Category.JsonUtil().createModelObjectPaging(getJson(), "categories");
    }

    public static final class Builder extends AbstractDataPagingRequest.Builder<Category, Builder> {
        public Builder(final String accessToken) {
            super(accessToken);
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
        public Builder limit(Integer limit) {
            assert (1 <= limit && limit <= 50);
            return setQueryParameter("limit", limit);
        }

        @Override
        public Builder offset(Integer offset) {
            assert (offset >= 0);
            return setQueryParameter("offset", offset);
        }

        @Override
        public GetListOfCategoriesRequest build() {
            setPath("/v1/browse/categories");
            return new GetListOfCategoriesRequest(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
