package it.ade.ma.api.sevice.covers;

import it.ade.ma.api.sevice.path.PathUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoversService {

    private final PathUtil pathUtil;

    public List<String> getAllCovers(String bandName) throws IOException {
        return pathUtil.getAllCovers(bandName)
                .collect(Collectors.toList());
    }

    public List<String> getAllScans(String bandName) throws IOException {
        return pathUtil.getAllScans(bandName)
                .collect(Collectors.toList());
    }

}
