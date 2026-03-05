package me.miki.shindo.gui.modmenu.category.list

/**
 * Shared content contract for ModMenu categories that are list pages.
 *
 * Contract covers:
 * - top filters/chips rendering;
 * - filtered entry rebuild;
 * - card layout metadata;
 * - card rendering pass;
 * - optional right-side details layer.
 */
interface ModMenuListPageContract {

    fun drawTopFilters(context: ModMenuListPageRenderContext): Float

    fun rebuildFilteredEntries(topFiltersBottom: Float)

    fun resolveCardLayoutSpec(): ModMenuListCardLayoutSpec

    fun drawEntryCards(context: ModMenuListPageRenderContext, layout: ModMenuListCardLayoutSpec)

    fun isDetailsLayerOpen(): Boolean = false

    fun drawDetailsLayer(context: ModMenuListPageRenderContext) {
        // Optional layer, default no-op.
    }
}

