package it.ade.ma.api.sevice.db.model.mapper;

import it.ade.ma.api.sevice.db.model.Band;
import it.ade.ma.api.sevice.db.model.dto.BandDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BandMapper {

    Band toBand(BandDTO bandDTO);

    BandDTO toBandDTO(Band band);

}
