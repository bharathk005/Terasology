/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.logic.inventory.events;

import org.terasology.entitySystem.event.Event;

/**
 * @author Marcin Sciesinski
 */
public class InventorySlotStackSizeChangedEvent implements Event {
    private int slot;
    private int oldSize;
    private int newSize;

    public InventorySlotStackSizeChangedEvent(int slot, int oldSize, int newSize) {
        this.slot = slot;
        this.oldSize = oldSize;
        this.newSize = newSize;
    }

    public int getSlot() {
        return slot;
    }

    public int getOldSize() {
        return oldSize;
    }

    public int getNewSize() {
        return newSize;
    }
}
