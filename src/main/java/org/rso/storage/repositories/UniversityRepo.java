package org.rso.storage.repositories;

import org.rso.storage.entities.University;
import org.rso.storage.types.Location;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Created by Radosław on 24.05.2016.
 */
public interface UniversityRepo extends MongoRepository<University,String> {
    University findByName(String name);
    List<University> findByLocation(Location location);
}
