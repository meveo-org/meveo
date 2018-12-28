package org.meveo.service.neo4j.scheduler;

import java.util.*;

public class AtomicPersistencePlan {

    /*
    TODO:
    We must always use a Set<EntityToPersist> to schedule it, that can either contain Node or Relations
    Order would be : leaf => target => loop(source => link => source ... => link ..) => non unique relationships
 */

    private List<List<EntityToPersist>> schedule = new ArrayList<>();

    public List<List<EntityToPersist>> getSchedule() {
        return schedule;
    }

    public void addEntity(EntityToPersist e){
        if(e != null){
            schedule.add(Collections.singletonList(e));
        }
    }

    public void addEntities(List<EntityToPersist> e){
        if(!e.isEmpty()){
            schedule.add(e);
        }
    }

    public List<EntityToPersist> get(int i){
        return schedule.get(i);
    }

    /**
     * Was created for easing the test reading
     *
     * @param i Index of the set of entity
     * @return An entity contained in the set at the given index
     */
    public EntityToPersist firstAtIndex(int i){
        return schedule.get(i).iterator().next();
    }

    /**
     * @return An iterator representing the persistence schedule. All the elements at each iteration
     * can be persisted at the same time.
     */
    public Iterator<List<EntityToPersist>> iterator() {
        return schedule.listIterator();
    }

}
