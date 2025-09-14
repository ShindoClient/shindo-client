package me.miki.shindo.libs.spotify.requests.data;

import me.miki.shindo.libs.spotify.model_objects.specification.Paging;
import me.miki.shindo.libs.spotify.requests.IRequest;

public interface IPagingRequestBuilder<T, BT extends IRequest.Builder<Paging<T>, ?>>
        extends IRequest.Builder<Paging<T>, BT> {
    BT limit(final Integer limit);

    BT offset(final Integer offset);
}
