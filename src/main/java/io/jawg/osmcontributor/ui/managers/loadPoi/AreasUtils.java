package io.jawg.osmcontributor.ui.managers.loadPoi;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import io.jawg.osmcontributor.BuildConfig;
import io.jawg.osmcontributor.model.entities.MapArea;
import io.jawg.osmcontributor.utils.Box;

public class AreasUtils {
    public static final BigDecimal GRANULARITY_LAT = new BigDecimal(BuildConfig.GRANULARITY_AREA);
    public static final BigDecimal GRANULARITY_LNG = new BigDecimal(BuildConfig.GRANULARITY_AREA);

    private AreasUtils() {
        //emtpty
    }

    public static List<MapArea> computeMapAreaOfBox(Box box) {
        List<MapArea> areas = new ArrayList<>();
        //a coup de modulo on calcul l'id

        BigDecimal north = new BigDecimal(box.getNorth());
        BigDecimal west = new BigDecimal(box.getWest());
        BigDecimal east = new BigDecimal(box.getEast());
        BigDecimal south = new BigDecimal(box.getSouth());

        long northArea = north.multiply(GRANULARITY_LAT).setScale(0, RoundingMode.HALF_UP).longValue();
        long westArea = west.multiply(GRANULARITY_LNG).setScale(0, RoundingMode.HALF_DOWN).longValue();
        long southArea = south.multiply(GRANULARITY_LNG).setScale(0, RoundingMode.HALF_DOWN).longValue();
        long eastArea = east.multiply(GRANULARITY_LNG).setScale(0, RoundingMode.HALF_UP).longValue();

        //we get the long up the area is rounded up for north and down for south
        for (int latInc = 0; latInc <= northArea - southArea; latInc++) {
            for (int lngInc = 0; lngInc <= eastArea - westArea; lngInc++) {
                long idLat = southArea + latInc;
                long idLng = westArea + lngInc;

                //ID is south west
                Long id = Long.valueOf(String.valueOf(idLat) + String.valueOf(idLng));

                areas.add(new MapArea(id,
                        (southArea + latInc + 1) / GRANULARITY_LAT.doubleValue(),
                        (southArea + latInc) / GRANULARITY_LAT.doubleValue(),
                        (westArea + lngInc + 1) / GRANULARITY_LAT.doubleValue(),
                        (westArea + lngInc) / GRANULARITY_LAT.doubleValue()));
            }
        }

        return areas;
    }


    public static List<Long> getIds(List<MapArea> areasNeeded) {
        List<Long> ids = new ArrayList<>();
        for (MapArea mapArea : areasNeeded) {
            ids.add(mapArea.getId());
        }
        return ids;
    }
}
