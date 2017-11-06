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
package io.jawg.osmcontributor.rest.utils;

import com.mapbox.mapboxsdk.exceptions.ConversionException;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import io.jawg.osmcontributor.utils.CloseableUtils;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

public class XMLConverterFactory extends Converter.Factory {
    private Serializer serializer;

    public XMLConverterFactory(Serializer serializer) {
        this.serializer = serializer;
    }

    public static XMLConverterFactory create(Persister persister) {
        return new XMLConverterFactory(persister);
    }


    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(final Type type, Annotation[] annotations, Retrofit retrofit) {
        return new Converter<ResponseBody, Object>() {
            @Override
            public Object convert(ResponseBody body) throws IOException {

                String charset = "UTF-8";

                InputStreamReader isr = null;
                try {
                    isr = new InputStreamReader(body.byteStream(), charset);
                    return serializer.read((Class<?>) type, isr);
                } catch (Exception e) {
                    throw new ConversionException(e);
                } finally {
                    CloseableUtils.closeQuietly(isr);
                }
            }
        };
    }
}