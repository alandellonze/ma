package it.ade.ma.api.repository;

import it.ade.ma.api.model.Band;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BandRepository extends CrudRepository<Band, Long> {

    Band findOneByName(String name);

}
