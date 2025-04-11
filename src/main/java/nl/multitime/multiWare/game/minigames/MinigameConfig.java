package nl.multitime.multiWare.game.minigames;

import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;

public class MinigameConfig implements ConfigurationSerializable {

    private final String name;
    private final String type;
    private boolean enabled;
    private Location spawnLocation;
    private int duration;
    private boolean pvpEnabled;
    private final Map<String, Object> extraData;

    public MinigameConfig(String name, String type) {
        this.name = name;
        this.type = type;
        this.enabled = true;
        this.duration = 60; // Standaard 60 seconden
        this.pvpEnabled = false; // Standaard geen PvP
        this.extraData = new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    public MinigameConfig(Map<String, Object> map) {
        this.name = (String) map.get("name");
        this.type = (String) map.get("type");
        this.enabled = (Boolean) map.get("enabled");
        this.spawnLocation = (Location) map.get("spawnLocation");
        this.duration = (Integer) map.get("duration");
        this.pvpEnabled = (Boolean) map.get("pvpEnabled");
        this.extraData = (Map<String, Object>) map.getOrDefault("extraData", new HashMap<>());
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

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public boolean isPvpEnabled() {
        return pvpEnabled;
    }

    public void setPvpEnabled(boolean pvpEnabled) {
        this.pvpEnabled = pvpEnabled;
    }

    public void setExtraData(String key, Object value) {
        extraData.put(key, value);
    }

    public Object getExtraData(String key) {
        return extraData.get(key);
    }

    public Object getExtraData(String key, Object defaultValue) {
        return extraData.getOrDefault(key, defaultValue);
    }

    public void removeExtraData(String key) {
        extraData.remove(key);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("type", type);
        map.put("enabled", enabled);
        map.put("spawnLocation", spawnLocation);
        map.put("duration", duration);
        map.put("pvpEnabled", pvpEnabled);
        map.put("extraData", extraData);
        return map;
    }

    public static MinigameConfig deserialize(Map<String, Object> map) {
        return new MinigameConfig(map);
    }

    public boolean isComplete() {
        return false;
    }
}
