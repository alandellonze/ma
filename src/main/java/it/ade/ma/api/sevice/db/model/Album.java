package it.ade.ma.api.sevice.db.model;

import it.ade.ma.api.constants.AlbumStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Album {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "band_id", nullable = false)
    private Band band;

    private Integer position;
    private String type;
    private Integer typeCount;
    private String name;
    private Integer year;

    @Enumerated
    private AlbumStatus status = AlbumStatus.NONE;

    private String maType;
    private Integer maTypeCount;
    private String maName;

}
