# Poweramp Lyrics Plugin Example
============================================

Poweramp v3 (build 948+) supports externally provided lyrics (both plain text and LRC) for the tracks played in the player.

The API is extremely simple and based on number of "broadcast" intents sent by Poweramp to your plugin and back from your plugin
to Poweramp. "Broadcast" term is quoted as we always send intent to the single specific receiver (either plugin app, or Powermap app).

The provided Plugin Example contains all the necessary code to handle lyrics, but it just generates fake lyrics data. The real plugin
may access network/cached database to get the actual lyrics content.