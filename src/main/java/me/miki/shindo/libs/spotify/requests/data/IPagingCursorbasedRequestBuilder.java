package me.miki.shindo.libs.spotify.requests.data;

import me.miki.shindo.libs.spotify.model_objects.specification.PagingCursorbased;
import me.miki.shindo.libs.spotify.requests.IRequest;

public interface IPagingCursorbasedRequestBuilder<T, A, BT extends IRequest.Builder<PagingCursorbased<T>, ?>>
        extends IRequest.Builder<PagingCursorbased<T>, BT> {
    BT limit(final Integer limit);

    BT after(final A after);
}
