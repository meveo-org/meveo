/*
 * (C) Copyright 2018-2019 Webdrone SAS (https://www.webdrone.fr/) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. This program is
 * not suitable for any direct or indirect application in MILITARY industry See the GNU Affero
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.meveo.persistence.scheduler;

import java.util.*;

public class AtomicPersistencePlan {

    /*
    TODO:
    We must always use a Set<ItemToPersist> to schedule it, that can either contain Node or Relations
    Order would be : leaf => target => loop(source => link => source ... => link ..) => non unique relationships
 */

    private List<Set<ItemToPersist>> schedule = new ArrayList<>();

    public List<Set<ItemToPersist>> getSchedule() {
        return schedule;
    }

    public void addEntity(ItemToPersist e){
        if(e != null){
            schedule.add(Collections.singleton(e));
        }
    }

    public void addEntities(Set<ItemToPersist> e){
        if(!e.isEmpty()){
            schedule.add(e);
        }
    }

    public Set<ItemToPersist> get(int i){
        return schedule.get(i);
    }

    /**
     * Was created for easing the test reading
     *
     * @param i Index of the set of entity
     * @return An entity contained in the set at the given index
     */
    public ItemToPersist firstAtIndex(int i){
        return schedule.get(i).iterator().next();
    }

    /**
     * @return An iterator representing the persistence schedule. All the elements at each iteration
     * can be persisted at the same time.
     */
    public Iterator<Set<ItemToPersist>> iterator() {
        return schedule.listIterator();
    }

}
