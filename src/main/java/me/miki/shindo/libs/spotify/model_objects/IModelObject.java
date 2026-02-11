package me.miki.shindo.libs.spotify.model_objects;

import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.miki.shindo.libs.spotify.model_objects.specification.Paging;
import me.miki.shindo.libs.spotify.model_objects.specification.PagingCursorbased;

import java.io.Serializable;
public interface IModelObject extends Serializable {
    Builder builder();
    @JsonPOJOBuilder(withPrefix = "set")
    interface Builder {
        IModelObject build();
    }
    interface IJsonUtil<T> {
        boolean hasAndNotNull(final JsonObject jsonObject, final String memberName);
        T createModelObject(final JsonObject jsonObject);
        T createModelObject(final String json);
        T[] createModelObjectArray(final JsonArray jsonArray);
        T[] createModelObjectArray(final String json);
        T[] createModelObjectArray(final String json, final String key);
        <X> X[] createModelObjectArray(final JsonArray jsonArray, final Class<X> clazz);
        Paging<T> createModelObjectPaging(final JsonObject jsonObject);
        Paging<T> createModelObjectPaging(final String json);
        Paging<T> createModelObjectPaging(final String json, final String key);
        PagingCursorbased<T> createModelObjectPagingCursorbased(final JsonObject jsonObject);
        PagingCursorbased<T> createModelObjectPagingCursorbased(final String json);
        PagingCursorbased<T> createModelObjectPagingCursorbased(final String json, final String key);
    }
}
