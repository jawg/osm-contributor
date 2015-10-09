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
package io.mapsquare.osmcontributor.sync;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import io.mapsquare.osmcontributor.sync.events.error.ServerNotAvailableEvent;
import io.mapsquare.osmcontributor.sync.events.error.SyncUnauthorizedEvent;
import io.mapsquare.osmcontributor.sync.events.error.TooManyRequestsEvent;
import retrofit.RetrofitError;
import timber.log.Timber;

public class OSMProxy {

    private EventBus eventBus;

    @Inject
    public OSMProxy(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public interface NetworkAction<T> {
        T proceed();
    }

    public interface Result<T> {
        boolean isSuccess();

        T getResult();

        RetrofitError getRetrofitError();
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

        @Override
        public RetrofitError getRetrofitError() {
            return null;
        }
    }

    public class Failure<T> implements Result<T> {

        final RetrofitError retrofitError;

        public Failure(RetrofitError retrofitError) {
            this.retrofitError = retrofitError;
        }

        public RetrofitError getRetrofitError() {
            return retrofitError;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public T getResult() {
            throw new IllegalStateException();
        }
    }

    protected <NA> Result<NA> proceed(NetworkAction<NA> action) throws RetrofitError {
        try {
            return new Success<>(action.proceed());

        } catch (RetrofitError e) {

            if (e.getKind() == RetrofitError.Kind.NETWORK) {
                Timber.e(e, "Retrofit error, connection lost");
                return new Failure<>(null);
            } else if (e.getResponse() != null) {
                switch (e.getResponse().getStatus()) {
                    case 401:
                        Timber.e(e, "Retrofit error, user unauthorized");
                        eventBus.post(new SyncUnauthorizedEvent());
                        return new Failure<>(null);

                    case 503:
                        Timber.e(e, "Retrofit error, server not available");
                        eventBus.post(new ServerNotAvailableEvent());
                        return new Failure<>(null);

                    case 429:
                        Timber.e(e, "Retrofit error, Too Many Requests");
                        eventBus.post(new TooManyRequestsEvent());
                        return new Failure<>(null);
                }
            }
            return new Failure<>(e);
        }
    }
}