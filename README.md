# HGS Query Plugin

Official Hytale server plugin for [HytaleGameServers.net](https://hytalegameservers.net) — the premier server listing platform for Hytale.

This plugin keeps your server listing on HytaleGameServers.net up to date by periodically sending server status, player counts, and other metadata to the API.

## ✨ Features

- 🔄 **Automatic Status Updates** — Real-time player counts, MOTD, world info, and more sent to your server listing
- 🌐 **Network Mode** — Run multiple Hytale servers as a single listing with automatic controller election and peer-to-peer TCP communication
- ⚡ **Fully Asynchronous** — All API calls and network I/O run on asynchronous threads with zero impact on gameplay
- 🔒 **Secure Communication** — HTTPS with Bearer token authentication
- 🛡️ **Privacy Controls** — Toggle what data is shared: player usernames, system environment, installed plugins
- 📊 **Dynamic Intervals** — 3 minute updates by default (1 minute for sponsored/real-time servers) — faster than the website's built-in Query Port polling

## 📦 Installation

1. Download the latest release from [Releases](https://github.com/HytaleGameServers/Query-Plugin/releases)
2. Place the JAR in your Hytale server's `mods/` directory
3. Start your server — configuration files will be generated in `mods/HGS_Query/`
4. Create an account and server listing on [HytaleGameServers.net](https://hytalegameservers.net)
5. Copy your **Server ID** and **API Token** from your server's dashboard
6. Paste them into `mods/HGS_Query/Api.json`
7. Restart your server

## ⚙️ Configuration

### `Api.json`

Your server credentials. Obtain these from your server dashboard on HytaleGameServers.net.

```json
{
  "serverId": "YOUR_SERVER_ID_HERE",
  "apiToken": "YOUR_API_TOKEN_HERE"
}
```

> 🔑 **Keep your API token private.** Do not share it publicly or commit it to version control.

### `Config.json`

General plugin settings and privacy controls.

| Setting | Default | Description                                                         |
|---|---|---------------------------------------------------------------------|
| `enabled` | `true` | Master toggle — set to `false` to disable all API communication     |
| `sendSystemEnvironmentName` | `true` | Include OS name (e.g. "Windows 10", "Linux") in updates             |
| `sendOnlinePlayerUsernames` | `true` | Include individual player usernames (player counts are always sent) |
| `sendPluginInfo` | `true` | Include installed plugin list (group, name and version only)        |

### `Network.json` (Multi-Server Only)

Required only if you're running multiple Hytale servers as a single listing.

| Setting | Default | Description |
|---|---|---|
| `enabled` | `false` | Enable network mode |
| `nodeId` | `"DEFAULT"` | Unique identifier for this node (e.g. `LOBBY`, `SURVIVAL-1`, `FACTIONS`) |
| `port` | `9800` | TCP port for inter-node communication |
| `nodes` | `[]` | List of peer addresses in `"host:port"` format (do not include this node) |

**How network mode works:**

Each node runs a TCP server and connects to all configured peers. Heartbeats track liveness every 5 seconds. The node with the lowest `nodeId` (lexicographic) is automatically elected as the **controller**. The controller collects snapshots from all peers and sends a single aggregated update to the HGS API.

If the controller goes down, a new controller is elected automatically — no manual intervention required.

**Example — 3 server network:**

On `LOBBY` (port 9800):
```json
{
  "enabled": true,
  "nodeId": "LOBBY",
  "port": 9800,
  "nodes": ["192.168.1.11:9800", "192.168.1.12:9800"]
}
```

On `SURVIVAL` (192.168.1.11):
```json
{
  "enabled": true,
  "nodeId": "SURVIVAL",
  "port": 9800,
  "nodes": ["192.168.1.10:9800", "192.168.1.12:9800"]
}
```

On `FACTIONS` (192.168.1.12):
```json
{
  "enabled": true,
  "nodeId": "FACTIONS",
  "port": 9800,
  "nodes": ["192.168.1.10:9800", "192.168.1.11:9800"]
}
```

> ⚠️ All nodes must share the same `serverId` and `apiToken` in their `Api.json`.

## 📋 Data Collected

The plugin sends the following to HytaleGameServers.net:

- Server name, MOTD, address, and port
- Current and maximum player count
- Online player usernames and UUIDs (if enabled)
- World names and count
- Server implementation version
- Installed plugins — group, name, and version only (if enabled)
- System environment name (if enabled)
- Password protection status (boolean only — no passwords are ever transmitted)
- Server boot timestamp

All data is automatically cleared when your server goes offline.

## 🔗 API Endpoints

The plugin communicates exclusively with:

- `POST https://hytalegameservers.net/api/plugin/query/update` — Status updates
- `POST https://hytalegameservers.net/api/plugin/query/shutdown` — Graceful shutdown notification

## ⏱️ Update Intervals

There are two ways your server listing stays up to date — the **plugin** (this project) and the **website's built-in Query Port polling**. The plugin is faster and takes priority.

| Method | Default Interval | Sponsored / Real-Time |
|---|---|---|
| **Plugin** (recommended) | 3 minutes | 1 minute |
| **Query Port** (website-side) | 5 minutes | 1 minute |

> When the plugin is actively syncing with the API, the website backend will **not** query your server's Query Port — the plugin takes over entirely. Query Port polling only applies to servers that don't have this plugin installed.

## 📝 Requirements

- Hytale Server (official release)
- Java 21+
- Account and server listing on [HytaleGameServers.net](https://hytalegameservers.net)
- Outbound HTTPS access to `hytalegameservers.net`

## 🔨 Building from Source

```bash
git clone https://github.com/HytaleGameServers/Query-Plugin.git
cd Plugin
mvn clean package
```

The compiled JAR will be in the `target/` directory.

## 💬 Support

- 🌐 **Website:** [hytalegameservers.net](https://hytalegameservers.net)
- 💬 **Discord:** [hytalegameservers.net/discord](https://hytalegameservers.net/discord)

## ⚠️ Important Notice

Abuse of the API or plugin is strictly prohibited. This includes attempting to bypass rate limits, sending falsified data, reverse engineering for malicious purposes, or any other violation of our [Terms of Service](https://hytalegameservers.net/terms-of-service). Violations will result in suspension or permanent termination of your server listing and account.

---

*This is an unofficial community project. Hytale is a trademark of Hypixel Studios.*
