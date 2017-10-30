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
package io.jawg.osmcontributor.database.helper;


import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.LazyForeignCollection;
import com.j256.ormlite.misc.TransactionManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;

/**
 * Provides utilities methods to make database calls and getting lazyloaded collections.
 */
public class DatabaseHelper {

    OsmSqliteOpenHelper helper;

    @Inject
    public DatabaseHelper(OsmSqliteOpenHelper helper) {
        this.helper = helper;
    }

    /**
     * Execute the code of the callable who can throw an unchecked exception.
     * If an exception is thrown, wrap it in a RuntimeException.
     *
     * @param throwingCode Code who can throw an exception.
     * @param <T>          Return type of the code.
     * @return Return of the code contained in the Callable.
     */
    public static <T> T wrapException(Callable<T> throwingCode) {
        try {
            return throwingCode.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Execute the code of the callable wrapped in a transaction and wrap the thrown exceptions in a RuntimeException.
     *
     * @param throwingTransactionCode Code to execute in a transaction.
     * @param <T>                     Return type of the code.
     * @return Return of the code contained in the Callable.
     */
    public <T> T callInTransaction(final Callable<T> throwingTransactionCode) {
        return wrapException(new Callable<T>() {
            @Override
            public T call() throws Exception {
                return TransactionManager.callInTransaction(helper.getConnectionSource(), throwingTransactionCode);
            }
        });
    }

    /**
     * Load a Collection who was not loaded du to lazyLoading.
     *
     * @param toLoad Collection to load.
     * @param <T>    Type of objects of the collection.
     * @return List of loaded objects.
     */
    public static <T> List<T> loadLazyForeignCollection(final Collection<T> toLoad) {
        return wrapException(new Callable<List<T>>() {
            @Override
            public List<T> call() throws Exception {
                ArrayList<T> result = new ArrayList<>();
                if (!(toLoad instanceof LazyForeignCollection)) {
                    throw new RuntimeException("not a lazyloaded collection");
                }
                CloseableIterator<T> iterator = ((LazyForeignCollection<T, Long>) toLoad).closeableIterator();
                try {
                    while (iterator.hasNext()) {
                        result.add(iterator.next());
                    }
                } finally {
                    iterator.close();
                }
                return result;
            }
        });
    }

}
