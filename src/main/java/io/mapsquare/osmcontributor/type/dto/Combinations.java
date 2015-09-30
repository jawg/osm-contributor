/**
 * Copyright (C) 2015 eBusiness Information
 *
 * This file is part of OSM Contributor.
 *
 * OSM Contributor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OSM Contributor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OSM Contributor.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.mapsquare.osmcontributor.type.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Combinations {
    Integer page;
    @SerializedName("rp")
    Integer responsePerPage;
    Integer total;
    String url;
    List<CombinationsData> data;

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getResponsePerPage() {
        return responsePerPage;
    }

    public void setResponsePerPage(Integer responsePerPage) {
        this.responsePerPage = responsePerPage;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<CombinationsData> getData() {
        return data;
    }

    public void setData(List<CombinationsData> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Combinations{");
        sb.append("page=").append(page);
        sb.append(", responsePerPage=").append(responsePerPage);
        sb.append(", total=").append(total);
        sb.append(", url='").append(url).append('\'');
        sb.append(", data=").append(data);
        sb.append('}');
        return sb.toString();
    }
}
