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

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

import javax.annotation.Nullable;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import retrofit2.Converter;
import retrofit2.Retrofit;

public class XMLConverterStringFactory extends Converter.Factory {
    private Serializer serializer;

    public XMLConverterStringFactory(Serializer serializer) {
        this.serializer = serializer;
    }

    public static XMLConverterStringFactory create(Persister persister) {
        return new XMLConverterStringFactory(persister);
    }

    @Nullable
    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
        return new Converter<String, RequestBody>() {
            @Override
            public RequestBody convert(final String value) throws IOException {
                return new RequestBody() {
                    @Nullable
                    @Override
                    public MediaType contentType() {
                        return MediaType.parse("text/plain; charset=utf-8");
                    }

                    @Override
                    public void writeTo(BufferedSink sink) throws IOException {
                        sink.write(value.getBytes(Charset.forName("UTF-8")));
                    }
                };
            }
        };
    }
}