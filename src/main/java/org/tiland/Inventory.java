package org.tiland;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Popup;

public class Inventory {
  

    public int capacity = 2;
    public Npc heldItem = null;

    List<Npc> items = new ArrayList<>();
    List<String> keys = new ArrayList<>();

    public Npc getHeldItem() {
        
        return heldItem; 
    }

    public boolean take(Npc npc, Zone zone) {

        if (items.size() == capacity) {

            // Toss the oldest thing on the ground. Safest and provides feedback.
            drop(0, false, zone);
        }

        // Add it to the inventory.
        items.add(npc);
    
        npc.flags.dynamic = false;
        npc.flags.collidable = false;

        // Remove it from the zone.
        npc.requestParent(null);
        npc.setZoneName(null);

        hold(items.size() - 1);
       
        return true;
    }

    public Npc drop(int itemIdx, boolean holdNewest, Zone zone) {

        int droppedItemIdx = -1;

        if (itemIdx != -1) {
            droppedItemIdx = itemIdx; 
        }
        
        else if (heldItem != null) {
            
            droppedItemIdx = findHeldItemIdx();
            heldItem = null;
        }

        else {
            return null;
        }

        Npc droppedItem = items.get(droppedItemIdx);

        // Put this back in the zone. It will leave the scene when the zone does.
        droppedItem.requestParent(zone.zoneRoot);
        droppedItem.setZoneName(zone.zoneName);

        droppedItem.flags.dynamic = true;
        droppedItem.flags.collidable = true;

        items.remove(droppedItemIdx);

        if (holdNewest && heldItem == null && items.size() > 0) {
            hold(items.size() - 1);
        }

        return droppedItem;
    }

    public void hold(int itemIdx) {

        if (heldItem != null) {
            heldItem.setVisible(false);
        }

        heldItem = items.get(itemIdx);
        heldItem.setVisible(true);
    }

    public String[] getInventoryNames() {

        String[] inventoryNames = new String[items.size()];

        for(int i = 0; i < items.size(); i++) {
            inventoryNames[i] = items.get(i).getName();
        }

        return inventoryNames;
    }

    public void removeInventoryItem(String itemName, Zone zone) {

        Npc cur;

        for (int i = 0; i < items.size(); i++) {

            cur = items.get(i);

            if (cur.getName().equals(itemName)) {

                if (cur == heldItem) {
                    heldItem.setVisible(false);
                    drop(-1, true, zone);
                    
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

    private int findHeldItemIdx() {

        if (heldItem == null) {
            return -1;
        }

        for (int i = 0; i < items.size(); i++) {

            if (items.get(i) == heldItem) {
                return i;
            }
        }

        return -1;
    }

    public boolean isFull() {
        return items.size() == capacity;
    }
}
