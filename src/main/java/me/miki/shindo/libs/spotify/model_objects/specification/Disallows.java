package me.miki.shindo.libs.spotify.model_objects.specification;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.miki.shindo.libs.spotify.enums.Action;
import me.miki.shindo.libs.spotify.model_objects.AbstractModelObject;
import me.miki.shindo.libs.spotify.model_objects.special.Actions;

import java.util.EnumSet;
import java.util.Map;
@JsonDeserialize(builder = Actions.Builder.class)
public class Disallows extends AbstractModelObject {
    private final EnumSet<Action> disallowedActions;

    public Disallows(Builder builder) {
        super(builder);
        this.disallowedActions = builder.disallowedActions;
    }
    public EnumSet<Action> getDisallowedActions() {
        return disallowedActions;
    }

    @Override
    public String toString() {
        return "Disallows(disallowedActions=" + disallowedActions + ")";
    }

    @Override
    public Builder builder() {
        return new Builder();
    }
    public static final class Builder extends AbstractModelObject.Builder {
        private EnumSet<Action> disallowedActions;
        public Builder setDisallowedActions(EnumSet<Action> disallowedActions) {
            this.disallowedActions = disallowedActions;
            return this;
        }

        @Override
        public Disallows build() {
            return new Disallows(this);
        }
    }
    public static final class JsonUtil extends AbstractModelObject.JsonUtil<Disallows> {
        @Override
        public Disallows createModelObject(JsonObject jsonObject) {
            if (jsonObject == null || jsonObject.isJsonNull()) {
                return null;
            }

            EnumSet<Action> disallowedActions = EnumSet.noneOf(Action.class);
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                if (entry.getValue().getAsJsonPrimitive().getAsBoolean()) {
                    disallowedActions.add(
                            Action.keyOf(entry.getKey().toLowerCase()));
                }
            }

            return new Builder()
                    .setDisallowedActions(
                            disallowedActions)
                    .build();
        }
    }

}
