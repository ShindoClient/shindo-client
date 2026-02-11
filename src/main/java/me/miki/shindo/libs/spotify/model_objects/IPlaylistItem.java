package me.miki.shindo.libs.spotify.model_objects;

import me.miki.shindo.libs.spotify.enums.ModelObjectType;
import me.miki.shindo.libs.spotify.model_objects.specification.Episode;
import me.miki.shindo.libs.spotify.model_objects.specification.ExternalUrl;
import me.miki.shindo.libs.spotify.model_objects.specification.Track;
public interface IPlaylistItem extends IModelObject {
    Integer getDurationMs();
    ExternalUrl getExternalUrls();
    String getHref();
    String getId();
    String getName();
    ModelObjectType getType();
    String getUri();
}
