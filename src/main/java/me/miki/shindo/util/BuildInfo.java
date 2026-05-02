package me.miki.shindo.util;

/**
 * Build information structure for Shindo Client.
 * Contains version and build identifiers following the new Build ID system.
 */
public class BuildInfo {
    private final int build;
    private final String semver;
    private final String buildId;
    private final String type;

    public BuildInfo(int build, String semver, String buildId, String type) {
        this.build = build;
        this.semver = semver;
        this.buildId = buildId;
        this.type = type;
    }

    public int getBuild() { return build; }
    public String getSemver() { return semver; }
    public String getBuildId() { return buildId; }
    public String getType() { return type; }

    public String getDisplayString() {
        return "Shindo Client v" + semver + " (" + buildId + " " + type + ")";
    }

    /**
     * Default stable build for 1.8.9
     */
    public static final BuildInfo DEFAULT = new BuildInfo(5111, "5.1.11", "5111.1", "stable");
}
