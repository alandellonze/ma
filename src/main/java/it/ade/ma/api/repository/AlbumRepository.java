package it.ade.ma.api.repository;

import it.ade.ma.api.model.Album;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlbumRepository extends CrudRepository<Album, Long> {

    List<Album> findAllByBandNameOrderByPositionAsc(String bandName);

}
