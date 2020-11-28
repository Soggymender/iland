package org.tiland;

import java.util.ArrayList;
import java.util.List;

public class Inventory {
  
    public Npc heldItem = null;

    List<Npc> items = new ArrayList<>();
    List<String> keys = new ArrayList<>();

    public Npc getHeldItem() {
        return heldItem; 
    }

    public boolean take(Npc npc) {

        if (heldItem != null) {
            return false;
        }

        // Add it to the inventory.
        items.add(npc);

        heldItem = npc;
        heldItem.flags.dynamic = false;
        heldItem.flags.collidable = false;

        // Remove it from the zone.
        heldItem.requestParent(null);
        heldItem.setZoneName(null);

        return true;
    }

    public Npc drop(Zone zone) {

        if (heldItem == null) {
            return null;
        }

        if (items.contains(heldItem)) {
            items.remove(heldItem);
        }

        // Put this back in the zone. It will leave the scene when the zone does.
        heldItem.requestParent(zone.zoneRoot);
        heldItem.setZoneName(zone.zoneName);

        heldItem.flags.dynamic = true;
        heldItem.flags.collidable = true;

        Npc droppedItem = heldItem;
        heldItem = null;

        return droppedItem;
    }

    public String[] getInventoryNames() {

        String[] inventoryNames = new String[items.size()];

        for(int i = 0; i < items.size(); i++) {
            inventoryNames[i] = items.get(i).getName();
        }

        return inventoryNames;
    }

    public void removeInventoryItem(String itemName, Zone zone) {

        for (int i = 0; i < items.size(); i++) {

            if (items.get(i).getName().equals(itemName)) {

                if (heldItem == items.get(i)) {
                    heldItem.setVisible(false);
                    drop(zone);
                    
                } else {
                    items.remove(i);
                }

                break;
            }
        }
    }

    public void addKey(String key) {
        keys.add(key);
    }

    public List<String> getKeys() {
        return keys;
    }
}
