package me.miki.shindo.libs.spotify.model_objects.specification;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.JsonObject;
import me.miki.shindo.libs.spotify.model_objects.AbstractModelObject;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
@JsonDeserialize(builder = Paging.Builder.class)
public class Paging<T> extends AbstractModelObject {
    private final String href;
    private final T[] items;
    private final Integer limit;
    private final String next;
    private final Integer offset;
    private final String previous;
    private final Integer total;

    private Paging(final Paging.Builder<T> builder) {
        super(builder);

        this.href = builder.href;
        this.items = builder.items;
        this.limit = builder.limit;
        this.next = builder.next;
        this.offset = builder.offset;
        this.previous = builder.previous;
        this.total = builder.total;
    }
    public String getHref() {
        return href;
    }
    public T[] getItems() {
        return items;
    }
    public Integer getLimit() {
        return limit;
    }
    public String getNext() {
        return next;
    }
    public Integer getOffset() {
        return offset;
    }
    public String getPrevious() {
        return previous;
    }
    public Integer getTotal() {
        return total;
    }

    @Override
    public String toString() {
        return "Paging(href=" + href + ", items=" + Arrays.toString(items) + ", limit=" + limit + ", next=" + next
                + ", offset=" + offset + ", previous=" + previous + ", total=" + total + ")";
    }

    @Override
    public Builder<T> builder() {
        return new Builder<>();
    }
    public static final class Builder<T> extends AbstractModelObject.Builder {
        private String href;
        private T[] items;
        private Integer limit;
        private String next;
        private Integer offset;
        private String previous;
        private Integer total;
        public Builder<T> setHref(String href) {
            this.href = href;
            return this;
        }
        public Builder<T> setItems(T[] items) {
            this.items = items;
            return this;
        }
        public Builder<T> setLimit(Integer limit) {
            this.limit = limit;
            return this;
        }
        public Builder<T> setNext(String next) {
            this.next = next;
            return this;
        }
        public Builder<T> setOffset(Integer offset) {
            this.offset = offset;
            return this;
        }
        public Builder<T> setPrevious(String previous) {
            this.previous = previous;
            return this;
        }
        public Builder<T> setTotal(Integer total) {
            this.total = total;
            return this;
        }

        @Override
        public Paging<T> build() {
            return new Paging<>(this);
        }
    }
    @SuppressWarnings("unchecked")
    public static final class JsonUtil<X> extends AbstractModelObject.JsonUtil<Paging<X>> {
        public Paging<X> createModelObject(JsonObject jsonObject) {
            if (jsonObject == null || jsonObject.isJsonNull()) {
                return null;
            }

            return new Paging.Builder<X>()
                    .setHref(
                            hasAndNotNull(jsonObject, "href")
                                    ? jsonObject.get("href").getAsString()
                                    : null)
                    .setItems(
                            createModelObjectArray(
                                    jsonObject.getAsJsonArray("items"), (Class<X>) ((ParameterizedType) getClass()
                                            .getGenericSuperclass()).getActualTypeArguments()[0]))
                    .setLimit(
                            hasAndNotNull(jsonObject, "limit")
                                    ? jsonObject.get("limit").getAsInt()
                                    : null)
                    .setNext(
                            hasAndNotNull(jsonObject, "next")
                                    ? jsonObject.get("next").getAsString()
                                    : null)
                    .setOffset(
                            hasAndNotNull(jsonObject, "offset")
                                    ? jsonObject.get("offset").getAsInt()
                                    : null)
                    .setPrevious(
                            hasAndNotNull(jsonObject, "previous")
                                    ? jsonObject.get("previous").getAsString()
                                    : null)
                    .setTotal(
                            hasAndNotNull(jsonObject, "total")
                                    ? jsonObject.get("total").getAsInt()
                                    : null)
                    .build();
        }
    }
}
