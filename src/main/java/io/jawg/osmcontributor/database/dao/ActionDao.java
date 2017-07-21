/**
 * Copyright (C) 2017 eBusiness Information
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
package io.jawg.osmcontributor.database.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.jawg.osmcontributor.model.entities.Action;

/**
 * Dao for {@link io.jawg.osmcontributor.model.entities.Action} objects.
 */
public class ActionDao extends RuntimeExceptionDao<Action, Long> {

    @Inject
    public ActionDao(Dao<Action, Long> dao) {
        super(dao);
    }

    public Action createIfNotExist(Action action) {
        if (action == null) {
            return action;
        }
        // Query for the source
        Map<String, Object> fields = new HashMap<>();
        fields.put(Action.KEY, action.getKey());
        fields.put(Action.TYPE, action.getType());
        fields.put(Action.VALUE, action.getValue());
        List<Action> db_data = queryForFieldValues(fields);
        Action db_action = db_data.size() > 0 ? db_data.get(0) : null;
        // Create the source doesn't exist, otherwise get the ID
        if (db_action == null) {
            createOrUpdate(action);
        } else {
            action.setId(db_action.getId());
        }
        return action;
    }
}
