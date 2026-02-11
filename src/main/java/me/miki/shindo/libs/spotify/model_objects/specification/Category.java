package me.miki.shindo.libs.spotify.model_objects.specification;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.JsonObject;
import me.miki.shindo.libs.spotify.model_objects.AbstractModelObject;

import java.util.Arrays;
import java.util.Objects;
@JsonDeserialize(builder = Category.Builder.class)
public class Category extends AbstractModelObject {
    private final String href;
    private final Image[] icons;
    private final String id;
    private final String name;

    private Category(final Builder builder) {
        super(builder);

        this.href = builder.href;
        this.icons = builder.icons;
        this.id = builder.id;
        this.name = builder.name;
    }
    public String getHref() {
        return href;
    }
    public Image[] getIcons() {
        return icons;
    }
    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Category(href=" + href + ", icons=" + Arrays.toString(icons) + ", id=" + id + ", name=" + name + ")";
    }

    @Override
    public Builder builder() {
        return new Builder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Category category = (Category) o;
        return Objects.equals(href, category.href) && Objects.equals(id, category.id) &&
                Objects.equals(name, category.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(href, id, name);
    }
    public static final class Builder extends AbstractModelObject.Builder {
        private String href;
        private Image[] icons;
        private String id;
        private String name;
        public Builder setHref(String href) {
            this.href = href;
            return this;
        }
        public Builder setIcons(Image... icons) {
            this.icons = icons;
            return this;
        }
        public Builder setId(String id) {
            this.id = id;
            return this;
        }
        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        @Override
        public Category build() {
            return new Category(this);
        }
    }
    public static final class JsonUtil extends AbstractModelObject.JsonUtil<Category> {
        public Category createModelObject(JsonObject jsonObject) {
            if (jsonObject == null || jsonObject.isJsonNull()) {
                return null;
            }

            return new Category.Builder()
                    .setHref(
                            hasAndNotNull(jsonObject, "href")
                                    ? jsonObject.get("href").getAsString()
                                    : null)
                    .setIcons(
                            hasAndNotNull(jsonObject, "icons")
                                    ? new Image.JsonUtil().createModelObjectArray(
                                    jsonObject.getAsJsonArray("icons"))
                                    : null)
                    .setId(
                            hasAndNotNull(jsonObject, "id")
                                    ? jsonObject.get("id").getAsString()
                                    : null)
                    .setName(
                            hasAndNotNull(jsonObject, "name")
                                    ? jsonObject.get("name").getAsString()
                                    : null)
                    .build();
        }
    }
}
