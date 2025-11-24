package me.miki.shindo.management.skin;

import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Renderer that relies on Mineatar's remote preview service instead of composing skins locally.
 * <p>
 * Example usage:
 * <pre>
 *     SkinPreviewRenderer renderer = new SkinPreviewRenderer();
 *     renderer.renderRemoteSkinPreview(
 *         vgContext,
 *         "8667ba71-b85a-4004-af54-457a9734eed7", // UUID (with or without hyphen)
 *         200f,
 *         140f,
 *         1.0f, // local scale multiplier (remote PNG already rendered with scale=8)
 *         new Color(0, 0, 0, 90),
 *         new Color(255, 255, 255, 120)
 *     );
 * </pre>
 */
public class SkinPreviewRenderer {

    private static final int REMOTE_PREVIEW_SCALE = 8;
    private static final int CONNECT_TIMEOUT_MS = 6000;
    private static final int READ_TIMEOUT_MS = 7000;
    private static final float DEFAULT_BASE_WIDTH = 132f;
    private static final float DEFAULT_BASE_HEIGHT = 276f;
    private static final String PREVIEW_URL_TEMPLATE = "https://api.mineatar.io/body/full/%s?scale=%d&overlay=true";

    private final Map<String, Integer> previewCache = new HashMap<>();

    private float baseWidth = DEFAULT_BASE_WIDTH;
    private float baseHeight = DEFAULT_BASE_HEIGHT;

    /**
     * @return Width of the last rendered remote preview (before GUI scale is applied).
     */
    public float getBaseWidth() {
        return baseWidth;
    }

    /**
     * @return Height of the last rendered remote preview (before GUI scale is applied).
     */
    public float getBaseHeight() {
        return baseHeight;
    }

    /**
     * Draws the remote preview fetched from Mineatar for the provided UUID.
     *
     * @param vg         NanoVG context pointer.
     * @param uuid       Player UUID (with or without hyphens).
     * @param x          Screen X where the preview should start (before applying {@code scale}).
     * @param y          Screen Y where the preview should start (before applying {@code scale}).
     * @param scale      Local scaling factor applied on top of the remote PNG resolution.
     * @param background Optional rounded background color (null to skip).
     * @param border     Optional rounded border color (null to skip).
     */
    public void renderRemoteSkinPreview(long vg,
                                        String uuid,
                                        float x, float y,
                                        float scale,
                                        Color background,
                                        Color border) {
        if (vg == 0 || scale <= 0f) {
            return;
        }

        String normalizedUuid = normalizeUuid(uuid);
        if (normalizedUuid == null) {
            return;
        }

        int imageHandle;
        try {
            imageHandle = getOrCreatePreviewImage(vg, normalizedUuid);
        } catch (IOException exception) {
            System.err.println("SkinPreviewRenderer: Failed to fetch preview for " + normalizedUuid + " (" + exception.getMessage() + ")");
            return;
        }

        if (imageHandle <= 0) {
            return;
        }

        int imageWidth;
        int imageHeight;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            NanoVG.nvgImageSize(vg, imageHandle, w, h);
            imageWidth = w.get(0);
            imageHeight = h.get(0);
        }

        if (imageWidth <= 0 || imageHeight <= 0) {
            return;
        }

        baseWidth = imageWidth;
        baseHeight = imageHeight;

        NanoVG.nvgSave(vg);
        NanoVG.nvgTranslate(vg, x, y);
        NanoVG.nvgScale(vg, scale, scale);

        if (background != null && background.getAlpha() > 0) {
            drawBackground(vg, background, imageWidth, imageHeight);
        }

        NVGPaint paint = NVGPaint.calloc();
        try {
            NanoVG.nvgBeginPath(vg);
            NanoVG.nvgRect(vg, 0, 0, imageWidth, imageHeight);
            NanoVG.nvgImagePattern(vg, 0, 0, imageWidth, imageHeight, 0f, imageHandle, 1f, paint);
            NanoVG.nvgFillPaint(vg, paint);
            NanoVG.nvgFill(vg);
        } finally {
            paint.free();
        }

        if (border != null && border.getAlpha() > 0) {
            drawBorder(vg, border, imageWidth, imageHeight);
        }

        NanoVG.nvgRestore(vg);
    }

    /**
     * @return {@code true} if we already downloaded/cached a preview for the UUID.
     */
    public boolean isPreviewCached(String uuid) {
        String normalized = normalizeUuid(uuid);
        return normalized != null && previewCache.containsKey(normalized);
    }

    /**
     * Removes a cached preview image and releases its NanoVG handle.
     */
    public void destroyCachedPreview(long vg, String uuid) {
        String normalized = normalizeUuid(uuid);
        if (vg == 0 || normalized == null) {
            return;
        }
        Integer handle = previewCache.remove(normalized);
        if (handle != null && handle > 0) {
            NanoVG.nvgDeleteImage(vg, handle);
        }
    }

    /**
     * Clears the entire preview cache and releases all NanoVG image handles.
     */
    public void clearCache(long vg) {
        if (vg == 0 && !previewCache.isEmpty()) {
            // Still clear the map to avoid leaking keys if context is gone.
            previewCache.clear();
            baseWidth = DEFAULT_BASE_WIDTH;
            baseHeight = DEFAULT_BASE_HEIGHT;
            return;
        }

        for (Integer handle : previewCache.values()) {
            if (handle != null && handle > 0 && vg != 0) {
                NanoVG.nvgDeleteImage(vg, handle);
            }
        }
        previewCache.clear();
        baseWidth = DEFAULT_BASE_WIDTH;
        baseHeight = DEFAULT_BASE_HEIGHT;
    }

    private int getOrCreatePreviewImage(long vg, String normalizedUuid) throws IOException {
        Integer cached = previewCache.get(normalizedUuid);
        if (cached != null && cached > 0) {
            return cached;
        }

        byte[] payload = downloadPreviewBytes(normalizedUuid);
        ByteBuffer buffer = MemoryUtil.memAlloc(payload.length);
        try {
            buffer.put(payload);
            buffer.flip();
            int imageHandle = NanoVG.nvgCreateImageMem(vg, NanoVG.NVG_IMAGE_NEAREST, buffer);
            if (imageHandle <= 0) {
                throw new IOException("nvgCreateImageMem returned " + imageHandle);
            }
            previewCache.put(normalizedUuid, imageHandle);
            return imageHandle;
        } finally {
            MemoryUtil.memFree(buffer);
        }
    }

    private byte[] downloadPreviewBytes(String normalizedUuid) throws IOException {
        HttpURLConnection connection = null;
        try {
            String url = String.format(Locale.ROOT, PREVIEW_URL_TEMPLATE, normalizedUuid, REMOTE_PREVIEW_SCALE);
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setRequestProperty("User-Agent", "ShindoClient/RemoteSkinPreview");
            connection.setUseCaches(false);

            int status = connection.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP " + status + " while requesting " + url);
            }

            try (InputStream inputStream = connection.getInputStream();
                 ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                }
                return outputStream.toByteArray();
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static String normalizeUuid(String uuid) {
        if (uuid == null) {
            return null;
        }
        String trimmed = uuid.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        String cleaned = trimmed.replace("-", "");
        return cleaned.isEmpty() ? null : cleaned.toLowerCase(Locale.ROOT);
    }

    private void drawBackground(long vg, Color color, float width, float height) {
        NVGColor nvgColor = NVGColor.calloc();
        try {
            NanoVG.nvgRGBA((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue(), (byte) color.getAlpha(), nvgColor);
            NanoVG.nvgBeginPath(vg);
            NanoVG.nvgRoundedRect(vg, 0, 0, width, height, 6f);
            NanoVG.nvgFillColor(vg, nvgColor);
            NanoVG.nvgFill(vg);
        } finally {
            nvgColor.free();
        }
    }

    private void drawBorder(long vg, Color color, float width, float height) {
        NVGColor nvgColor = NVGColor.calloc();
        try {
            NanoVG.nvgRGBA((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue(), (byte) color.getAlpha(), nvgColor);
            NanoVG.nvgBeginPath(vg);
            NanoVG.nvgRoundedRect(vg, 0, 0, width, height, 6f);
            NanoVG.nvgStrokeColor(vg, nvgColor);
            NanoVG.nvgStrokeWidth(vg, 0.9f);
            NanoVG.nvgStroke(vg);
        } finally {
            nvgColor.free();
        }
    }
}
