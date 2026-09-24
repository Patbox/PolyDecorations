# PolyDecorations
Wanted to decorate your world bit more? Add some sign posts directing through the world,
some benches in the park to sit on or shelves in your house to put items on? 

PolyDecorations is Fabric (and Quilt) server side modification powered by 
[Polymer](https://modrinth.com/mod/polymer) which multiple decorative blocks such as wooden benches,
shelves, sign posts and braziers! It's ideal to add to server you play with your friends,
that don't want to install mods on the client.

![](https://cdn.modrinth.com/data/5710VC7f/images/6f8ae29b5268773c5bb48268c5f806ba902c3698.png)

## [== Download on Modrinth ==](https://modrinth.com/mod/polydecorations)

This mod was heavily inspired by [Aurora's Decorations](https://modrinth.com/mod/aurorasdecorations) mod,
which you should try out/use instead if you aren't looking for server side mod!

## Configuration
Features can be configured in `config/polydecorations.json` (created on first start, with everything enabled).
Every feature can be set to one of these values:
- `true` - enabled (default).
- `false` - its recipes are removed, but its blocks and items still exist, so nothing gets removed from existing worlds.
  Features without recipes (`wall_lanterns`, `fence_leads`) stop working instead.
- `"hard"` - removed completely. Its blocks, items and entities aren't registered, it doesn't use
  any Polymer block states and its assets aren't added to the resource pack.
  Blocks and items of this feature are removed from existing worlds!

```json
{
  "features": {
    "bench": false,
    "statues": "hard"
  }
}
```

Features: `canvas`, `mailbox`, `sign_post`, `rope`, `hammer`, `trowel`, `wall_lanterns` (placing lanterns on walls),
`fence_leads` (tying a lead to a fence without a leashed mob), `shelf`, `bench`, `table`, `tool_rack`, `stump`, `sleeping_bag`,
`brazier`, `copper_campfire`, `globe`, `display_case`, `flower_pots`, `ghost_lights`, `trashcan`, `basket`, `cardboard_box`,
`wind_chime`, `statues` and `tied_containers` (tying containers with string to hide their contents).

## Extra mods you might want to use!

### Polymer AutoHost
Simplest way to handle automatic resource pack generation for your server!
While bundled by main distribution of [Polymer](https://modrinth.com/mod/polymer), you need to enable it by hand
as it will be otherwise disabled. But it is quick and simple!

### Armor Stand Editor
The best™ server side pose editor for Armor Stand (and by extension, Statues!). Highly suggested for better customizability!
[Checkout Modrinth page for more information!](https://modrinth.com/mod/armorstandeditor)

### PolyFactory
The first of my mods in this style! But instead being purely decorative, it adds some functional stuff, new crafting stations
and automation. [Checkout Modrinth page for more information!](https://modrinth.com/mod/polyfactory)
