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
package io.jawg.osmcontributor.rest.security;

import android.util.Base64;

import com.github.scribejava.core.model.Verb;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Set;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;


public class SecurityUtils {

    /*=========================================*/
    /*-------------CONSTANTS-------------------*/
    /*=========================================*/
    private static final String SEPARATOR = "&";

    private static final String EQUAL = "=";


    /*=========================================*/
    /*------------UTILS METHOD-----------------*/
    /*=========================================*/

    /**
     * All flickr request must be signed. The signature is obtained from a concatenation of
     * elements in the url. See convertUrl method.
     *
     * @param convertedRequestUrl converted url
     * @return HMAC-SHA1 signature
     */
    public static String getSignatureFromRequest(String convertedRequestUrl, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(key.getBytes(), "HmacSHA1"));
            byte[] digest = mac.doFinal(convertedRequestUrl.getBytes());
            return new String(Base64.encode(digest, Base64.DEFAULT));
        } catch (NoSuchAlgorithmException | InvalidKeyException exception) {
            return null;
        }
    }

    /**
     * This method must be called before signing the request. The converted request url is obtained
     * by concatenation of elemments of the url. The converted url contains the HTTP verb, the base
     * url and all the parameters sorted by alphabetical order
     *
     * @param baseUrl  base url for the request
     * @param httpVerb request type (GET or POST)
     * @param params   request params (key : param's name, value : param's value
     * @return A converted url with following format: GET&url&param with escaped characters
     */
    public static String convertUrl(String baseUrl, Verb httpVerb, Map<String, String> params) {
        try {
            StringBuilder urlBuilder = new StringBuilder(httpVerb.name())
                    .append(SEPARATOR)
                    .append(URLEncoder.encode(baseUrl, "UTF-8"))
                    .append(SEPARATOR);

            StringBuilder paramsBuilder = new StringBuilder();
            Set<Map.Entry<String, String>> values = params.entrySet();
            for (Map.Entry<String, String> param : values) {
                paramsBuilder.append(param.getKey()).append(EQUAL).append(param.getValue()).append(SEPARATOR);
            }

            String paramsEncoded = URLEncoder.encode(paramsBuilder.deleteCharAt(paramsBuilder.lastIndexOf(SEPARATOR)).toString(), "UTF-8");
            return urlBuilder.append(paramsEncoded).toString();
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }
}
