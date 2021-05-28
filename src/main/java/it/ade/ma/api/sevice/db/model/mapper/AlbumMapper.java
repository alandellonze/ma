package it.ade.ma.api.sevice.db.model.mapper;

import it.ade.ma.api.sevice.db.model.Album;
import it.ade.ma.api.sevice.db.model.dto.AlbumDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface AlbumMapper {

    AlbumMapper INSTANCE = Mappers.getMapper(AlbumMapper.class);

    Album toAlbum(AlbumDTO albumDTO);

    List<Album> toAlbums(Iterable<AlbumDTO> albumDTOs);

    AlbumDTO toAlbumDTO(Album album);

    List<AlbumDTO> toAlbumDTOs(Iterable<Album> albums);

}
