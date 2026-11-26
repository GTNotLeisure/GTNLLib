package com.gtnllib.gui.theme.ae2;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.gtnllib.GTNLLib;
import com.gtnllib.gui.theme.AE2Theme;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class AE2ThemeLoader {

    /** Index contributed by enabled resource packs; its theme identifiers are additive. */
    public static final ResourceLocation INDEX = new ResourceLocation(GTNLLib.MODID, "themes/ae2/themes.json");
    private static final Pattern IDENTIFIER = Pattern.compile("[a-z0-9_.-]+:[a-z0-9_./-]+");
    private static final Pattern COLOR = Pattern.compile("#[0-9a-fA-F]{6}([0-9a-fA-F]{2})?");
    private static final Pattern TEXTURE_NAME = Pattern.compile("[a-z0-9_]+");

    private AE2ThemeLoader() {}

    /**
     * Loads built-in and custom skins in dependency order. An invalid definition and its dependents are skipped.
     * Every enabled index contributes identifiers; Minecraft chooses the highest-priority definition for each one.
     *
     * @param resources active client resource manager
     * @return a fresh list beginning with the three built-in skins
     */
    public static List<AE2Variant> load(IResourceManager resources) {
        Map<String, AE2Variant> loaded = new LinkedHashMap<>();
        Set<String> reserved = new HashSet<>();
        for (AE2Variant variant : AE2Variant.values()) {
            loaded.put(variant.id, variant);
            reserved.add(variant.id);
            reserved.add(variant.menuId);
        }
        Set<String> builtInIds = Set.copyOf(reserved);
        Set<String> ids = indexes(resources);
        reserved.addAll(ids);
        Map<String, Definition> definitions = new LinkedHashMap<>();
        Map<String, String> menus = new HashMap<>();
        for (String id : ids) {
            try {
                if (builtInIds.contains(id))
                    throw new JsonParseException("Cannot replace a built-in panel or menu identifier");
                ResourceLocation name = location(id);
                ResourceLocation source = new ResourceLocation(
                    name.getResourceDomain(),
                    "themes/ae2/" + name.getResourcePath() + ".json");
                JsonObject json = read(resources.getResource(source));
                String parent = text(json, "parent", AE2Theme.DARK_ID);
                String menu = text(json, "menu", id + "_menu");
                location(parent);
                location(menu);
                if (reserved.contains(menu) || menus.containsKey(menu))
                    throw new JsonParseException("Menu identifier is already reserved: " + menu);
                menus.put(menu, id);
                definitions.put(id, new Definition(json, parent, menu));
            } catch (IOException | RuntimeException error) {
                GTNLLib.LOG.warn("Could not read AE2 theme '{}': {}", id, error.getMessage());
            }
        }
        Set<String> visiting = new LinkedHashSet<>(), failed = new HashSet<>();
        for (String id : definitions.keySet()) resolve(id, definitions, loaded, visiting, failed, resources);
        return new ArrayList<>(loaded.values());
    }

    private static Set<String> indexes(IResourceManager resources) {
        Set<String> ids = new LinkedHashSet<>();
        List<IResource> indexes;
        try {
            indexes = resources.getAllResources(INDEX);
        } catch (FileNotFoundException absent) {
            return ids;
        } catch (IOException error) {
            GTNLLib.LOG.warn("Could not find AE2 theme indexes at {}", INDEX, error);
            return ids;
        }
        for (IResource resource : indexes) {
            try {
                JsonElement themes = read(resource).get("themes");
                if (themes == null || !themes.isJsonArray()) throw new JsonParseException("Expected a 'themes' array");
                for (JsonElement theme : themes.getAsJsonArray()) {
                    try {
                        if (!theme.isJsonPrimitive() || !theme.getAsJsonPrimitive()
                            .isString()) throw new JsonParseException("Theme identifiers must be strings");
                        String id = theme.getAsString();
                        location(id);
                        ids.add(id);
                    } catch (RuntimeException error) {
                        GTNLLib.LOG.warn("Invalid AE2 theme index entry {}: {}", theme, error.getMessage());
                    }
                }
            } catch (IOException | RuntimeException error) {
                GTNLLib.LOG.warn("Could not read AE2 theme index at {}: {}", INDEX, error.getMessage());
            }
        }
        return ids;
    }

    private static @Nullable AE2Variant resolve(String id, Map<String, Definition> definitions,
        Map<String, AE2Variant> loaded, Set<String> visiting, Set<String> failed, IResourceManager resources) {
        AE2Variant existing = loaded.get(id);
        if (existing != null || failed.contains(id)) return existing;
        Definition definition = definitions.get(id);
        if (definition == null) return null;
        if (!visiting.add(id)) {
            GTNLLib.LOG.warn("Circular AE2 theme inheritance: {} -> {}", visiting, id);
            return null;
        }
        try {
            AE2Variant parent = resolve(definition.parent(), definitions, loaded, visiting, failed, resources);
            if (parent == null) throw new JsonParseException("Missing or invalid parent: " + definition.parent());
            AE2Variant variant = build(id, definition, parent, resources);
            loaded.put(id, variant);
            return variant;
        } catch (IOException | RuntimeException error) {
            failed.add(id);
            GTNLLib.LOG.warn("Could not load AE2 theme '{}': {}", id, error.getMessage());
            return null;
        } finally {
            visiting.remove(id);
        }
    }

    private static AE2Variant build(String id, Definition definition, AE2Variant parent, IResourceManager resources)
        throws IOException {
        JsonObject json = definition.json();
        AE2Variant.Builder builder = AE2Variant.builder(id, parent)
            .menu(definition.menu())
            .colors(colors(object(json, "colors"), parent.colors))
            .highlight(
                color(json, "highlight", parent.highlight),
                color(json, "highlightTop", parent.highlightTop),
                color(json, "highlightShadow", parent.highlightShadow),
                color(json, "highlightText", parent.highlightText));
        if (json.has("dark")) {
            JsonElement dark = json.get("dark");
            if (!dark.isJsonPrimitive() || !dark.getAsJsonPrimitive()
                .isBoolean()) throw new JsonParseException("'dark' must be a boolean");
            builder.dark(dark.getAsBoolean());
        }
        for (Map.Entry<String, JsonElement> entry : object(json, "textures").entrySet()) {
            if (!TEXTURE_NAME.matcher(entry.getKey())
                .matches()) throw new JsonParseException("Invalid texture name: " + entry.getKey());
            String value = text(entry.getValue(), "texture " + entry.getKey());
            ResourceLocation texture = location(value);
            if (!texture.getResourcePath()
                .endsWith(".png")) throw new JsonParseException("Texture must name a PNG file: " + value);
            try (InputStream ignored = resources.getResource(texture)
                .getInputStream()) {
                builder.texture(entry.getKey(), texture);
            }
        }
        return builder.build();
    }

    private static AE2Colors colors(JsonObject json, AE2Colors parent) {
        return AE2Colors.builder()
            .text(color(json, "text", parent.text()))
            .muted(color(json, "muted", parent.muted()))
            .secondary(color(json, "secondary", parent.secondary()))
            .buttonText(color(json, "buttonText", parent.buttonText()))
            .hoverText(color(json, "hoverText", parent.hoverText()))
            .disabledText(color(json, "disabledText", parent.disabledText()))
            .fieldText(color(json, "fieldText", parent.fieldText()))
            .placeholder(color(json, "placeholder", parent.placeholder()))
            .line(color(json, "line", parent.line()))
            .edge(color(json, "edge", parent.edge()))
            .panel(color(json, "panel", parent.panel()))
            .well(color(json, "well", parent.well()))
            .outline(color(json, "outline", parent.outline()))
            .raised(color(json, "raised", parent.raised()))
            .good(color(json, "good", parent.good()))
            .bad(color(json, "bad", parent.bad()))
            .info(color(json, "info", parent.info()))
            .warning(color(json, "warning", parent.warning()))
            .error(color(json, "error", parent.error()))
            .build();
    }

    private static int color(JsonObject json, String key, int fallback) {
        if (!json.has(key)) return fallback;
        String value = text(json.get(key), key);
        if (!COLOR.matcher(value)
            .matches()) throw new JsonParseException("'" + key + "' must be #RRGGBB or #AARRGGBB");
        long color = Long.parseLong(value.substring(1), 16);
        return (int) (value.length() == 7 ? color | 0xFF000000L : color);
    }

    private static ResourceLocation location(String value) {
        if (!IDENTIFIER.matcher(value)
            .matches() || value.contains("..")) throw new JsonParseException("Invalid resource identifier: " + value);
        return new ResourceLocation(value);
    }

    private static String text(JsonObject json, String key, String fallback) {
        return json.has(key) ? text(json.get(key), key) : fallback;
    }

    private static String text(JsonElement value, String key) {
        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive()
            .isString()) throw new JsonParseException("'" + key + "' must be a string");
        return value.getAsString();
    }

    private static JsonObject object(JsonObject json, String key) {
        if (!json.has(key)) return new JsonObject();
        JsonElement value = json.get(key);
        if (!value.isJsonObject()) throw new JsonParseException("'" + key + "' must be an object");
        return value.getAsJsonObject();
    }

    private static JsonObject read(IResource resource) throws IOException {
        try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            JsonElement json = new JsonParser().parse(reader);
            if (!json.isJsonObject()) throw new JsonParseException("Expected a JSON object");
            return json.getAsJsonObject();
        }
    }

    private record Definition(JsonObject json, String parent, String menu) {}
}
