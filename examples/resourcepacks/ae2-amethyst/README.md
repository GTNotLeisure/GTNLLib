# Amethyst AE2 theme

Copy this directory into `.minecraft/resourcepacks`, enable it and reload resources with F3+T. No other mod or texture download is needed. The pack adds `example:amethyst` and the generated menu theme `example:amethyst_menu`.

`amethyst.json` lists every semantic and selection color. The icon override points at an existing GTNLLib texture so the example is self-contained; replace it with a PNG in your own namespace to ship custom artwork. Every omitted sheet inherits from the parent. Colors do not recolor PNG pixels.

The files in this example are not part of the mod's built-in resources and do not activate a theme unless installed as a resource pack. See `AE2_THEME.md` in the repository root for the format and texture dimensions.
