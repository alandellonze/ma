package it.ade.ma.api.repository;

import it.ade.ma.api.model.entity.Band;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BandRepository extends CrudRepository<Band, Long> {

    List<Band> findAllByMaKeyNotNullOrderByName();

    Band findOneByName(String name);

}
