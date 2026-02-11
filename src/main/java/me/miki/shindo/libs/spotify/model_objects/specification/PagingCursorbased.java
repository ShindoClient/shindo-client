package me.miki.shindo.libs.spotify.model_objects.specification;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.JsonObject;
import me.miki.shindo.libs.spotify.model_objects.AbstractModelObject;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
@JsonDeserialize(builder = PagingCursorbased.Builder.class)
public class PagingCursorbased<T> extends AbstractModelObject {
    private final String href;
    private final T[] items;
    private final Integer limit;
    private final String next;
    private final Cursor[] cursors;
    private final Integer total;

    private PagingCursorbased(final PagingCursorbased.Builder<T> builder) {
        super(builder);

        this.href = builder.href;
        this.items = builder.items;
        this.limit = builder.limit;
        this.next = builder.next;
        this.cursors = builder.cursors;
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
    public Cursor[] getCursors() {
        return cursors;
    }
    public Integer getTotal() {
        return total;
    }

    @Override
    public String toString() {
        return "PagingCursorbased(href=" + href + ", items=" + Arrays.toString(items) + ", limit=" + limit + ", next="
                + next + ", cursors=" + Arrays.toString(cursors) + ", total=" + total + ")";
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
        private Cursor[] cursors;
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
        public Builder<T> setCursors(Cursor... cursors) {
            this.cursors = cursors;
            return this;
        }
        public Builder<T> setTotal(Integer total) {
            this.total = total;
            return this;
        }

        @Override
        public PagingCursorbased<T> build() {
            return new PagingCursorbased<>(this);
        }
    }
    @SuppressWarnings("unchecked")
    public static final class JsonUtil<X> extends AbstractModelObject.JsonUtil<PagingCursorbased<X>> {
        public PagingCursorbased<X> createModelObject(JsonObject jsonObject) {
            if (jsonObject == null || jsonObject.isJsonNull()) {
                return null;
            }

            return new Builder<X>()
                    .setHref(
                            hasAndNotNull(jsonObject, "href")
                                    ? jsonObject.get("href").getAsString()
                                    : null)
                    .setItems(
                            hasAndNotNull(jsonObject, "items")
                                    ? createModelObjectArray(
                                    jsonObject.getAsJsonArray("items"), (Class<X>) ((ParameterizedType) getClass()
                                            .getGenericSuperclass()).getActualTypeArguments()[0])
                                    : null)
                    .setLimit(
                            hasAndNotNull(jsonObject, "limit")
                                    ? jsonObject.get("limit").getAsInt()
                                    : null)
                    .setNext(
                            hasAndNotNull(jsonObject, "next")
                                    ? jsonObject.get("next").getAsString()
                                    : null)
                    .setCursors(
                            hasAndNotNull(jsonObject, "cursors")
                                    ? new Cursor.JsonUtil().createModelObjectArray(
                                    jsonObject.getAsJsonArray("cursors"))
                                    : null)
                    .setTotal(
                            hasAndNotNull(jsonObject, "total")
                                    ? jsonObject.get("total").getAsInt()
                                    : null)
                    .build();
        }
    }
}
