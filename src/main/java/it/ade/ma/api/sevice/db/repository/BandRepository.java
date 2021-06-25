package it.ade.ma.api.sevice.db.repository;

import it.ade.ma.api.sevice.db.model.Band;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BandRepository extends CrudRepository<Band, Long> {

    List<Band> findAllByMaKeyNotNullOrderByName();

    @Query(nativeQuery = true, value = "SELECT * FROM Band WHERE UPPER(UNACCENT(name)) = UPPER(UNACCENT(:name))")
    Band findOneByName(String name);

}
