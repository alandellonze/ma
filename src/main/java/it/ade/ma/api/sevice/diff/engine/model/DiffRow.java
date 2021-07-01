package it.ade.ma.api.sevice.diff.engine.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiffRow<T> {

    public enum DiffType {
        EQUAL,
        PLUS,
        MINUS,
        CHANGE
    }

    private DiffType type;
    private List<T> original;
    private List<T> revised;

}
