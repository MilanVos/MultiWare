package nl.multitime.multiWare.game.minigames;

import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;

public class MinigameConfig implements ConfigurationSerializable {

    private String name;
    private String type;
    private boolean enabled;
    private Location spawnLocation;
    private Map<String, Object> extraData;

    public MinigameConfig(String name, String type) {
        this.name = name;
        this.type = type;
        this.enabled = false;
        this.extraData = new HashMap<>();
    }

    public MinigameConfig(Map<String, Object> map) {
        this.name = (String) map.get("name");
        this.type = (String) map.get("type");
        this.enabled = (Boolean) map.getOrDefault("enabled", false);
        this.spawnLocation = (Location) map.get("spawnLocation");

        this.extraData = new HashMap<>();
        Map<String, Object> extraMap = (Map<String, Object>) map.get("extraData");
        if (extraMap != null) {
            this.extraData.putAll(extraMap);
        }
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("type", type);
        map.put("enabled", enabled);
        map.put("spawnLocation", spawnLocation);
        map.put("extraData", extraData);
        return map;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }

    public void setSpawnLocation(Location spawnLocation) {
        this.spawnLocation = spawnLocation;
    }

    public Object getExtraData(String key) {
        return extraData.get(key);
    }

    public void setExtraData(String key, Object value) {
        extraData.put(key, value);
    }

    public boolean isComplete() {
        if (spawnLocation == null) {
            return false;
        }

        switch (type) {
            case "clickpressureplate":
                return extraData.containsKey("pressurePlateLocation");

            case "breakblock":
                return extraData.containsKey("arenaMin") && extraData.containsKey("arenaMax");
            case "towerup":
                return  extraData.containsKey("towerup");
            case "freeze":
                return extraData.containsKey("freeze");

            default:
                return true;
        }
    }
}
