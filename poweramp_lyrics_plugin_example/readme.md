# Poweramp Lyrics Plugin Example
============================================

Poweramp v3 (build 948+) supports externally provided lyrics (both plain and LRC) for the tracks played in the player.

The API is extremely simple and based on number of "broadcasted" intents sent by Poweramp to your plugin and back from your plugin
to Poweramp. "Broadcasted" term is quoted as we always send intent to the single specific receiver (either plugin app, or Powermap app).
