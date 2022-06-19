# Auxuji AuthX

## Overview
A simple authorization plugin for Velocity with no heavy dependencies. Password input is automatically hashed to SHA-256, and entries are saved to a JSON file in the plugin's data directory.

## Runtime Dependencies
```
floodgate - GeyserMC Floodgate API
```

## Build
Requires maven and git.
```
git clone https://github.com/koukuno/auxuji-authx-velocity.git
cd auxuji-authx-velocity
mvn package
```

## Permissions
```
auxuji-authx.add
auxuji-authx.change
auxuji-authx.delete
auxuji-authx.list
auxuji-authx.login
auxuji-authx.reload
auxuji-authx.save
```

## Velocity Commands
```
authx-add [username] [password] - Add an entry to database, or change an existing one. The password is hashed automatically.
authx-change [username] [password] - Change an existing entry in the database. The password is hashed automatically.
authx-delete [username] - Delete an existing entry from the database.
authx-list - List entries in the database.
authx-login - Login (should be configured for players, logouts are handled automatically.)
authx-reload - Reload plugin config and data.
authx-save - Save all entries.
```

## License
GPLv2-only, view LICENSE.txt for licensing terms
