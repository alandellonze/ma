package it.ade.ma.api.sevice.db.model.mapper;

import it.ade.ma.api.sevice.db.model.Album;
import it.ade.ma.api.sevice.db.model.dto.AlbumDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AlbumMapper {

    Album toAlbum(AlbumDTO albumDTO);

    AlbumDTO toAlbumDTO(Album album);

}
