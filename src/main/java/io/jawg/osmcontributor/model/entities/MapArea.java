/**
 * Copyright (C) 2016 eBusiness Information
 * <p>
 * This file is part of OSM Contributor.
 * <p>
 * OSM Contributor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OSM Contributor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OSM Contributor.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.jawg.osmcontributor.model.entities;


import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.joda.time.DateTime;

import io.jawg.osmcontributor.utils.Box;

@DatabaseTable(tableName = MapArea.TABLE_NAME)
public class MapArea {


    //les ids de ces objs ? sois on calcul un id en fonction de la position, un peut a la mode des tuiles


    public static final String TABLE_NAME = "AREA";

    public static final String ID = "ID";
    public static final String NORTH = "NORTH";
    public static final String SOUTH = "SOUTH";
    public static final String EAST = "EAST";
    public static final String WEAST = "WEAST";
    public static final String UPDATE_DATE = "UPDATE_DATE";


    @DatabaseField(columnName = ID, canBeNull = false, id = true)
    private String id;

    @DatabaseField(columnName = NORTH, canBeNull = false)
    private Double north;

    @DatabaseField(columnName = SOUTH, canBeNull = false)
    private Double south;

    @DatabaseField(columnName = EAST, canBeNull = false)
    private Double east;

    @DatabaseField(columnName = WEAST, canBeNull = false)
    private Double weast;

    @DatabaseField(columnName = UPDATE_DATE, canBeNull = false)
    private DateTime updateDate;

    public MapArea() {
    }

    public MapArea(String id, Double north, Double south, Double east, Double weast) {
        this.id = id;
        this.north = north;
        this.south = south;
        this.east = east;
        this.weast = weast;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Double getNorth() {
        return north;
    }

    public void setNorth(Double north) {
        this.north = north;
    }

    public Double getSouth() {
        return south;
    }

    public void setSouth(Double south) {
        this.south = south;
    }

    public Double getEast() {
        return east;
    }

    public void setEast(Double east) {
        this.east = east;
    }

    public Double getWeast() {
        return weast;
    }

    public void setWeast(Double weast) {
        this.weast = weast;
    }

    public DateTime getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(DateTime updateDate) {
        this.updateDate = updateDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MapArea mapArea = (MapArea) o;

        return id.equals(mapArea.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public Box getBox() {
        return new Box(north, east, south, weast);
    }
}
