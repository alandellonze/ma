package it.ade.ma.api.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WebPageAlbum {

    private String type;
    private String name;
    private String year;

}
