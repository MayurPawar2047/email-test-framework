package com.qa.email.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

/**
 * Central configuration access.
 *
 * Lookup order for every key (first non-blank wins):
 *   1. Environment variable  (key upper-cased, dots -> underscores, e.g. MAILOSAUR_API_KEY)
 *   2. JVM system property   (-Dmailosaur.api.key=...)
 *   3. config-{env}.properties  (env chosen with -Denv=qa or TEST_ENV, default "qa")
 *   4. config.properties
 *
 * Secrets (API key) must come from 1, 2 or a git-ignored config-local.properties - never from git.
 */
public final class ConfigManager {

    private static final Properties PROPS = new Properties();
    private static final String ENV = resolveEnv();

    static {
        load("config.properties", true);
        load("config-" + ENV + ".properties", false);
        load("config-local.properties", false); // git-ignored, for developer machines
    }

    private ConfigManager() {
    }

    public static String env() {
        return ENV;
    }

    public static Optional<String> optional(String key) {
        String envVar = System.getenv(key.toUpperCase().replace('.', '_'));
        if (isSet(envVar)) {
            return Optional.of(envVar.trim());
        }
        String sys = System.getProperty(key);
        if (isSet(sys)) {
            return Optional.of(sys.trim());
        }
        String file = PROPS.getProperty(key);
        return isSet(file) ? Optional.of(file.trim()) : Optional.empty();
    }

    public static String get(String key) {
        return optional(key).orElseThrow(() -> new IllegalStateException(
                "Missing required config '" + key + "'. Set env var "
                        + key.toUpperCase().replace('.', '_') + " or add it to config-local.properties"));
    }

    public static String get(String key, String defaultValue) {
        return optional(key).orElse(defaultValue);
    }

    public static int getInt(String key, int defaultValue) {
        return optional(key).map(Integer::parseInt).orElse(defaultValue);
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        return optional(key).map(Boolean::parseBoolean).orElse(defaultValue);
    }

    /** True when the Mailosaur credentials needed by integration tests are present. */
    public static boolean hasMailosaurCredentials() {
        return optional("mailosaur.api.key").isPresent() && optional("mailosaur.server.id").isPresent();
    }

    private static String resolveEnv() {
        String fromProp = System.getProperty("env");
        if (isSet(fromProp)) {
            return fromProp.trim();
        }
        String fromEnv = System.getenv("TEST_ENV");
        return isSet(fromEnv) ? fromEnv.trim() : "qa";
    }

    private static void load(String resource, boolean required) {
        try (InputStream in = ConfigManager.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                if (required) {
                    throw new IllegalStateException("Required config file not found on classpath: " + resource);
                }
                return;
            }
            PROPS.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + resource, e);
        }
    }

    private static boolean isSet(String s) {
        return s != null && !s.isBlank();
    }
}
