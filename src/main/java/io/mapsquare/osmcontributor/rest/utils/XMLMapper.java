/**
 * Copyright (C) 2016 eBusiness Information
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
package io.mapsquare.osmcontributor.rest.utils;

import com.mobprofs.retrofit.converters.SimpleXmlConverter;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;

import io.mapsquare.osmcontributor.utils.CloseableUtils;
import retrofit.converter.ConversionException;
import retrofit.mime.MimeUtil;
import retrofit.mime.TypedInput;
import timber.log.Timber;

public class XMLMapper extends SimpleXmlConverter {

    private Serializer serializer;

    /**
     * Constructs a XMLMapper using an instance of {@link org.simpleframework.xml.core.Persister} as serializer.
     */
    public XMLMapper() {
        this.serializer = new Persister();
    }

    /**
     * Constructs a XMLMapper using the given serializer.
     *
     * @param serializer custom serializer
     */
    public XMLMapper(Serializer serializer) {
        this.serializer = serializer;
    }

    @Override
    public Object fromBody(TypedInput body, Type type) throws ConversionException {
        String charset = "UTF-8";

        if (body.mimeType() != null) {
            charset = MimeUtil.parseCharset(body.mimeType());
        }

        if (body.mimeType().equals("text/plain; charset=utf-8")) {
            BufferedReader reader = null;
            StringBuilder sb = new StringBuilder();

            try {
                reader = new BufferedReader(new InputStreamReader(body.in()));

                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            } catch (IOException e) {
                Timber.e(e, "Exception while reading XML");
            } finally {
                CloseableUtils.closeQuietly(reader);
            }

            return sb.toString();
        }

        InputStreamReader isr = null;
        try {
            isr = new InputStreamReader(body.in(), charset);
            return serializer.read((Class<?>) type, isr);
        } catch (Exception e) {
            throw new ConversionException(e);
        } finally {
            CloseableUtils.closeQuietly(isr);
        }
    }

}