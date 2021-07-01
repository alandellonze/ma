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
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlbumService {

    private final AlbumMapper albumMapper;
    private final AlbumRepository albumRepository;

    public List<AlbumDTO> findAllByBandId(long bandId) {
        return albumRepository.findAllByBandIdOrderByPosition(bandId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<AlbumDTO> findById(Long id) {
        return albumRepository.findById(id)
                .map(this::convertToDTO);
    }

    public void save(AlbumDTO albumDTO) {
        Album album = convertToEntity(albumDTO);
        album = albumRepository.save(album);
        log.info("new album saved with id: {}", album.getId());
    }

    public void delete(long id) {
        albumRepository.deleteById(id);
        log.info("album with id: {} successfully deleted", id);
    }

    public void adjustPositions(long bandId) {
        adjustPositions(bandId, 1, 0);
    }

    public void adjustPositions(long bandId, int start, int offset) {
        log.info("adjustPositions({}, {}, {})", bandId, start, offset);

        // get all the Albums
        List<Album> albums = albumRepository.findAllByBandIdOrderByPosition(bandId);

        // adjust position
        for (int i = start - 1; i < albums.size(); i++) {
            Album album = albums.get(i);
            Integer position = i + 1 + offset;
            if (!Objects.equals(position, album.getPosition())) {
                log.debug("{}, {}: {} -> {} - {}", album.getBand().getName(), i, album.getPosition(), position, album.getName());
                album.setPosition(position);
                albumRepository.save(album);
            }
        }
    }

    // MAPPERS

    private AlbumDTO convertToDTO(Album album) {
        AlbumDTO albumDTO = albumMapper.toAlbumDTO(album);
        albumDTO.setBandId(album.getBand().getId());
        albumDTO.setBandName(album.getBand().getName());
        return albumDTO;
    }

    private Album convertToEntity(AlbumDTO albumDTO) {
        Album album = albumMapper.toAlbum(albumDTO);
        album.setBand(new Band(albumDTO.getBandId(), albumDTO.getBandName(), null));
        return album;
    }

}
