package it.ade.ma.api.sevice.db.service;

import it.ade.ma.api.sevice.db.model.Album;
import it.ade.ma.api.sevice.db.model.Band;
import it.ade.ma.api.sevice.db.model.dto.AlbumDTO;
import it.ade.ma.api.sevice.db.model.mapper.AlbumMapper;
import it.ade.ma.api.sevice.db.repository.AlbumRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlbumService {

    private final AlbumRepository albumRepository;

    private AlbumDTO convertToDTO(Album album) {
        if (album != null) {
            AlbumDTO albumDTO = AlbumMapper.INSTANCE.toAlbumDTO(album);
            albumDTO.setBandId(album.getBand().getId());
            albumDTO.setBandName(album.getBand().getName());
            return albumDTO;
        }
        return null;
    }

    private Album convertToEntity(AlbumDTO albumDTO) {
        if (albumDTO != null) {
            Album album = AlbumMapper.INSTANCE.toAlbum(albumDTO);
            album.setBand(new Band(albumDTO.getBandId(), albumDTO.getBandName(), null));
            return album;
        }
        return null;
    }

    public List<AlbumDTO> findAllByBandName(String bandName) {
        List<Album> albums = albumRepository.findAllByBandNameOrderByPositionAsc(bandName);
        return albums.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public AlbumDTO findById(Long id) {
        Optional<Album> album = albumRepository.findById(id);
        return album.map(this::convertToDTO).orElse(null);
    }

    public void save(AlbumDTO albumDTO) {
        Album album = convertToEntity(albumDTO);
        albumRepository.save(album);
    }

    public void delete(Long id) {
        albumRepository.deleteById(id);
    }

    public void adjustPositions(String bandName) {
        adjustPositions(bandName, 1, 0);
    }

    public void adjustPositions(String bandName, Integer start, Integer offset) {
        log.info("adjustPositions({}, {}, {})", bandName, start, offset);

        // get all the Albums
        List<Album> albums = albumRepository.findAllByBandNameOrderByPositionAsc(bandName);

        // adjust position
        for (int i = start - 1; i < albums.size(); i++) {
            Album album = albums.get(i);
            Integer position = i + 1 + offset;
            if (!position.equals(album.getPosition())) {
                log.debug("{}, {}: {} -> {} - {}", bandName, i, album.getPosition(), position, album.getName());
                album.setPosition(position);
                albumRepository.save(album);
            }
        }
    }

}
