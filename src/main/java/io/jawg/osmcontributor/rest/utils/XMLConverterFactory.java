/**
 * Copyright (C) 2019 Takima
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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import io.jawg.osmcontributor.utils.CloseableUtils;
import io.jawg.osmcontributor.utils.FlavorUtils;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

public class XMLConverterFactory extends Converter.Factory {
    private Serializer serializer;

    private static final String REPLACE_BLOCK_BY_OPENING_XML = "(<block id=\"[13579]\"/>)|(<block id=\".*?(?!\")\\d[13579]\"/>)";
    private static final String REPLACE_BLOCK_BY_CLOSING_XML = "(<block id=\"[02468]\"/>)|(<block id=\".*?(?!\")\\d[02468]\"/>)";
    private static final String OPENING_BLOCK = "<block>";
    private static final String CLOSING_BLOCK = "</block>";

    private XMLConverterFactory(Serializer serializer) {
        this.serializer = serializer;
    }

    public static XMLConverterFactory create(Persister persister) {
        return new XMLConverterFactory(persister);
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(final Type type, Annotation[] annotations, Retrofit retrofit) {
        return (Converter<ResponseBody, Object>) body -> {

            String charset = "UTF-8";

            InputStreamReader isr = null;
            InputStream str;

            /*
             * Use ByteArrayInputStream instead of byteStream to be able to get nodes and their relations with only one request.
             * Example of use: bus stop and its bus lines
             */
            String stream = filterBlocks(body.string());
            str = new ByteArrayInputStream(stream.getBytes());

            try {
                isr = new InputStreamReader(str, charset);
                return serializer.read((Class<?>) type, isr);
            } catch (Exception e) {
                throw new ConversionException(e);
            } finally {
                CloseableUtils.closeQuietly(isr);
            }
        };
    }

    /**
     * The filters enable the string to be parsed and to associate a relation to a node
     * The filter replaces
     *
     * <block id=xx1/>
     * <node></node>
     * <relation></relation>
     * <block id=xx2/>
     *
     * by
     *
     * <block>
     *     <node></node>
     *     <relation></relation>
     * </block>
     *
     * @param body the response
     * @return the xml parsable response
     */
    private static String filterBlocks(String body) {
        String temp = body.replaceAll(REPLACE_BLOCK_BY_CLOSING_XML, CLOSING_BLOCK);
        return temp.replaceAll(REPLACE_BLOCK_BY_OPENING_XML, OPENING_BLOCK);
    }
}
