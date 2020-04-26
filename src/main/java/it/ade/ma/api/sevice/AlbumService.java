package it.ade.ma.api.sevice;

import it.ade.ma.api.model.entity.Album;
import it.ade.ma.api.model.entity.Band;
import it.ade.ma.api.model.dto.AlbumDTO;
import it.ade.ma.api.repository.AlbumRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AlbumService {

    private final static Logger logger = LoggerFactory.getLogger(AlbumService.class);

    private ModelMapper modelMapper;
    private AlbumRepository albumRepository;

    @Autowired
    public void setModelMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Autowired
    public void setAlbumRepository(AlbumRepository albumRepository) {
        this.albumRepository = albumRepository;
    }

    private AlbumDTO convertToDTO(Album album) {
        if (album != null) {
            AlbumDTO albumDTO = modelMapper.map(album, AlbumDTO.class);
            albumDTO.setBandId(album.getBand().getId());
            albumDTO.setBandName(album.getBand().getName());
            return albumDTO;
        }
        return null;
    }

    private Album convertToEntity(AlbumDTO albumDTO) {
        if (albumDTO != null) {
            Album album = modelMapper.map(albumDTO, Album.class);
            album.setBand(new Band(albumDTO.getBandId(), albumDTO.getBandName(), null));
            return album;
        }
        return null;
    }

    List<AlbumDTO> findAllByBandName(String bandName) {
        List<Album> albums = albumRepository.findAllByBandNameOrderByPositionAsc(bandName);
        return albums.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    AlbumDTO findById(Long id) {
        Optional<Album> album = albumRepository.findById(id);
        return album.isPresent() ? convertToDTO(album.get()) : null;
    }

    void save(AlbumDTO albumDTO) {
        Album album = convertToEntity(albumDTO);
        albumRepository.save(album);
    }

    void delete(Long id) {
        albumRepository.deleteById(id);
    }

    void adjustPositions(String bandName) {
        adjustPositions(bandName, 1, 0);
    }

    void adjustPositions(String bandName, Integer start, Integer offset) {
        logger.info("adjustPositions({}, {}, {})", bandName, start, offset);

        // get all the Albums
        List<Album> albums = albumRepository.findAllByBandNameOrderByPositionAsc(bandName);

        // adjust position
        for (int i = start - 1; i < albums.size(); i++) {
            Album album = albums.get(i);
            Integer position = i + 1 + offset;
            if (!position.equals(album.getPosition())) {
                logger.debug("{}, {}: {} -> {} - {}", bandName, i, album.getPosition(), position, album.getName());
                album.setPosition(position);
                albumRepository.save(album);
            }
        }
    }

}
