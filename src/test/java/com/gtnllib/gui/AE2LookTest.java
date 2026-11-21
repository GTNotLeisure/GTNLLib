package com.gtnllib.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.gtnllib.gui.theme.AE2Theme;
import com.gtnllib.gui.theme.ae2.AE2Colors;
import com.gtnllib.gui.theme.ae2.AE2Look;
import com.gtnllib.gui.theme.ae2.AE2Look.Marker;
import com.gtnllib.gui.theme.ae2.AE2Scrollbars;
import com.gtnllib.gui.theme.ae2.AE2Variant;
import com.gtnllib.gui.widget.HandleScrollData;

public class AE2LookTest {

    @Test
    public void scrollWidthFollowsLook() {
        HandleScrollData large = new HandleScrollData(), small = new HandleScrollData(true);
        int vanilla = AE2Scrollbars.scrollbarWidth(null);
        assertEquals(vanilla, large.getThickness(), "a new list scroll bar starts plain");
        assertEquals(vanilla, small.getThickness(), "the small handle is an AE2 handle; plain looks use vanilla");
        large.look(AE2Look.of(AE2Variant.DARK));
        small.look(AE2Look.of(AE2Variant.DARK));
        assertEquals(AE2Theme.BIG_SCROLLER_WIDTH, large.getThickness(), "an AE2 panel gives the large AE2 handle");
        assertEquals(AE2Theme.SMALL_SCROLLER_WIDTH, small.getThickness(), "an AE2 panel gives the small AE2 handle");
        small.look(AE2Look.plain());
        assertEquals(vanilla, small.getThickness(), "a plain panel takes the vanilla width back");
    }

    @Test
    public void skinsByFolder() {
        for (AE2Variant variant : AE2Variant.values())
            assertSame(variant, AE2Look.skin(variant.folder), "the folder name finds its skin: " + variant.folder);
        assertNull(AE2Look.skin(""), "an empty name is the plain look");
        assertNull(AE2Look.skin("metal"), "a folder that is not an AE2 skin is the plain look");
        assertNull(AE2Look.skin(null), "no name is the plain look");
    }

    @Test
    public void sharedLooks() {
        assertSame(AE2Look.plain(), AE2Look.of((AE2Variant) null), "no skin gives the shared plain look");
        assertFalse(
            AE2Look.plain()
                .isAE2(),
            "the plain look is not an AE2 skin");
        for (AE2Variant variant : AE2Variant.values()) {
            AE2Look look = AE2Look.of(variant);
            assertSame(look, AE2Look.of(variant), "each skin has one shared look: " + variant);
            assertSame(variant, look.variant(), "the look keeps its skin: " + variant);
            assertTrue(look.isAE2(), "a skin's look is an AE2 look: " + variant);
        }
    }

    @Test
    public void markerInherits() {
        Marker parent = new Marker(AE2Variant.DARK.folder);
        JsonObject named = new JsonObject();
        named.addProperty(Marker.SKIN, AE2Variant.LIGHT.folder);
        assertEquals(
            AE2Variant.LIGHT.folder,
            new Marker(parent, named, null).skin(),
            "a theme that names a skin uses it");
        assertEquals(
            AE2Variant.DARK.folder,
            new Marker(parent, new JsonObject(), null).skin(),
            "a theme that names no skin keeps its parent's");
    }

    @Test
    public void listColors() {
        for (AE2Variant variant : AE2Variant.values()) {
            AE2Look look = AE2Look.of(variant);
            assertEquals(variant.colors.text(), look.listText(), "AE2 rows use the skin's text color: " + variant);
            assertEquals(variant.colors.bad(), look.bad(), "AE2 rows use the skin's bad color: " + variant);
            assertEquals(look.hover(), look.nested(), "AE2 skins shade nested rows like hovered ones: " + variant);
            assertFalse(look.listShadow(), "AE2 list text has no shadow: " + variant);
        }
        assertNotEquals(
            AE2Look.of(AE2Variant.LIGHT)
                .hover(),
            AE2Look.of(AE2Variant.DARK)
                .hover(),
            "hover darkens a light skin and lightens a dark one");
        AE2Look plain = AE2Look.plain();
        assertTrue(plain.listShadow(), "plain list text has Minecraft's shadow on the grey slot");
        assertNotEquals(plain.listText(), plain.listMuted(), "plain headers stand apart from row text");
    }

    @Test
    public void colorsByName() {
        AE2Colors colors = AE2Colors.builder()
            .text(1)
            .muted(2)
            .secondary(3)
            .buttonText(4)
            .hoverText(5)
            .disabledText(6)
            .fieldText(7)
            .placeholder(8)
            .line(9)
            .edge(10)
            .panel(11)
            .well(12)
            .outline(13)
            .raised(14)
            .good(15)
            .bad(16)
            .info(17)
            .warning(18)
            .error(19)
            .build();
        assertEquals(
            new AE2Colors(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19),
            colors,
            "each named setter fills its own component");
    }

    @Test
    public void purpleTextures() throws IOException {
        Path assets = Paths.get("src/main/resources/assets/gtnllib"), skins = assets.resolve("textures/gui/skin");
        assertEquals(
            AE2Theme.PURPLE_TEXTURES,
            names(skins.resolve(AE2Theme.PURPLE_FOLDER)),
            "the purple folder holds exactly the textures it recolours");
        for (String name : names(skins.resolve(AE2Theme.DARK_FOLDER))) {
            Path resolved = assets.resolve(
                AE2Theme.texture(AE2Theme.PURPLE_FOLDER, name)
                    .getResourcePath());
            assertTrue(Files.isRegularFile(resolved), "the purple skin finds " + name + " at " + resolved);
        }
    }

    private static Set<String> names(Path folder) throws IOException {
        try (Stream<Path> files = Files.list(folder)) {
            return files.map(
                file -> file.getFileName()
                    .toString()
                    .replace(".png", ""))
                .collect(Collectors.toSet());
        }
    }
}
