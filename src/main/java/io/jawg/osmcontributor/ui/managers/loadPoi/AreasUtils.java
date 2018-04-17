package io.jawg.osmcontributor.ui.managers.loadPoi;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import io.jawg.osmcontributor.BuildConfig;
import io.jawg.osmcontributor.model.entities.MapArea;
import io.jawg.osmcontributor.utils.Box;

public class AreasUtils {
    public static final BigDecimal GRANULARITY_LAT = new BigDecimal(BuildConfig.MAP_AREA_FACTOR);
    public static final BigDecimal GRANULARITY_LNG = new BigDecimal(BuildConfig.MAP_AREA_FACTOR);

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

        long northArea = north.multiply(GRANULARITY_LAT).setScale(0, north.compareTo(BigDecimal.ZERO) > 0 ? RoundingMode.UP : RoundingMode.DOWN).longValue();
        long westArea = west.multiply(GRANULARITY_LNG).setScale(0, west.compareTo(BigDecimal.ZERO) > 0 ? RoundingMode.DOWN : RoundingMode.UP).longValue();
        long southArea = south.multiply(GRANULARITY_LNG).setScale(0, south.compareTo(BigDecimal.ZERO) > 0 ? RoundingMode.DOWN : RoundingMode.UP).longValue();
        long eastArea = east.multiply(GRANULARITY_LNG).setScale(0, east.compareTo(BigDecimal.ZERO) > 0 ? RoundingMode.UP : RoundingMode.DOWN).longValue();


        //we get the long up the area is rounded up for north and down for south
        for (int latInc = 0; latInc <= northArea - southArea - 1; latInc++) {
            for (int lngInc = 0; lngInc <= eastArea - westArea - 1; lngInc++) {
                long idLat = southArea + latInc;
                long idLng = westArea + lngInc;

                //ID is south west
                String id = String.valueOf(idLat) + String.valueOf(idLng);

                areas.add(new MapArea(id,
                        (southArea + latInc + 1) / GRANULARITY_LAT.doubleValue(),
                        (southArea + latInc) / GRANULARITY_LAT.doubleValue(),
                        (westArea + lngInc + 1) / GRANULARITY_LAT.doubleValue(),
                        (westArea + lngInc) / GRANULARITY_LAT.doubleValue()));
            }
        }

        return areas;
    }


    public static List<String> getIds(List<MapArea> areasNeeded) {
        List<String> ids = new ArrayList<>();
        for (MapArea mapArea : areasNeeded) {
            ids.add(mapArea.getId());
        }
        return ids;
    }
}
