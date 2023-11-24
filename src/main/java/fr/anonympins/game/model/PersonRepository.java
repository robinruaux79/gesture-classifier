package fr.anonympins.game.model;

import fr.anonympins.game.model.entity.GameObject;
import fr.anonympins.game.model.entity.Person;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PersonRepository extends JpaRepository<Person, Long> {

    @Query(nativeQuery = false,
            value = "SELECT p FROM Person p WHERE p.id =(select max(f.id) from Person f) ORDER BY SQRT((p.location.x-:x)*(p.location.x-:x)+(p.location.y-:y)*(p.location.y-:y)) ASC ")
    List<Person> findNearest(@Param("x") double x,@Param("y")  double y, Pageable limit);
}
