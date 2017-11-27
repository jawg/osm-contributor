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
package io.jawg.osmcontributor.rest;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

public class OSMProxy {

    private EventBus eventBus;

    @Inject
    public OSMProxy(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public <NA> Result<NA> proceed(NetworkAction<NA> action) {
        return new Success<>(action.proceed());
    }

    public interface NetworkAction<T> {
        T proceed();
    }

    public interface Result<T> {
        boolean isSuccess();

        T getResult();
    }

    public class Success<T> implements Result<T> {
        final T result;

        public Success(T result) {
            this.result = result;
        }

        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public T getResult() {
            return result;
        }
    }

    public class Failure<T> implements Result<T> {

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public T getResult() {
            throw new IllegalStateException();
        }
    }
}