package org.tiland;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Popup;

public class Inventory {
  

    public int capacity = 2;
    public int heldItemIdx = -1;

    List<Npc> items = new ArrayList<>();
    List<String> keys = new ArrayList<>();

    public Npc getHeldItem() {
        
        if (heldItemIdx == -1) {
            return null;
        }

        return items.get(heldItemIdx); 
    }

    public boolean take(Npc npc) {

        if (heldItemIdx != -1) {
            return false;
        }

        // Add it to the inventory.
        items.add(npc);

        heldItemIdx = items.size() - 1;
    
        npc.flags.dynamic = false;
        npc.flags.collidable = false;

        // Remove it from the zone.
        npc.requestParent(null);
        npc.setZoneName(null);

        return true;
    }

    public Npc drop(Zone zone) {

        if (heldItemIdx == -1) {
            return null;
        }

        Npc droppedItem = items.get(heldItemIdx);

        // Put this back in the zone. It will leave the scene when the zone does.
        droppedItem.requestParent(zone.zoneRoot);
        droppedItem.setZoneName(zone.zoneName);

        droppedItem.flags.dynamic = true;
        droppedItem.flags.collidable = true;

        items.remove(heldItemIdx);
        heldItemIdx = -1;

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

                if (i == heldItemIdx) {
                    items.get(heldItemIdx).setVisible(false);
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
