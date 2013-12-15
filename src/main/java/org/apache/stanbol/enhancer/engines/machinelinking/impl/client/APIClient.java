/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.stanbol.enhancer.engines.machinelinking.impl.client;

import org.apache.commons.io.IOUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * This class defines a client for the <i>MachineLinking API</i>.
 *
 * @author Michele Mostarda (michele@machinelinking.com)
 */
public class APIClient {

    private static final Logger log = LoggerFactory.getLogger(APIClient.class);

    private static final String DEFAULT_SERVICE_URL = "http://api.machinelinking.com";

    private static final int DEFAULT_CONN_TIMEOUT = 60 * 1000;

    private static final String LANG_SERVICE = "/lang/";

    private static final String ANNOTATE_SERVICE = "/annotate/";

    private static final String COMPARE_SERVICE = "/compare/";

    private static final String SUMMARY_SERVICE = "/summary/";

    private final String serviceURL;
    private final int connTimeout;

    private final String appId;
    private final String appKey;

    public APIClient(String appId, String appKey, String serviceURL, int connTimeout) {
        this.appId = appId;
        this.appKey = appKey;
        this.serviceURL = serviceURL;
        this.connTimeout = connTimeout;
    }

    public APIClient(String appId, String appKey, int connTimeout) {
        this(appId, appKey, DEFAULT_SERVICE_URL, connTimeout);
    }

    public APIClient(String appId, String appKey) {
        this(appId, appKey, DEFAULT_CONN_TIMEOUT);
    }

    public GuessedLanguageResponse guessLanguage(String textToAnalyze) throws IOException, APIClientException {
        final Map<String,Object> properties = new HashMap<String,Object>();
        properties.put(ParamsValidator.text, URLEncoder.encode(textToAnalyze, "UTF8"));
        InputStream is = null;
        try {
            is = sendRequest(serviceURL + LANG_SERVICE, "lang", properties);
            return parseLangResponse(is);
        } finally {
            if(is != null) is.close();
        }
    }

    public AnnotationResponse annotate(String textToAnalyze) throws IOException, APIClientException {
        final Map<String,Object> properties = new HashMap<String,Object>();
        properties.put(ParamsValidator.text, URLEncoder.encode(textToAnalyze, "UTF8"));
        final InputStream is = sendRequest(serviceURL + ANNOTATE_SERVICE, "annotate", properties);
        return parseAnnotateResponse(is);
    }

    public CompareResponse compare(ComparisonMethod compareMethod, String text1, String text2)
    throws IOException, APIClientException {
        final Map<String,Object> properties = new HashMap<String,Object>();
        properties.put(ParamsValidator.func, compareMethod.name());
        properties.put(ParamsValidator.text1, text1);
        properties.put(ParamsValidator.text2, text2);
        final InputStream is = sendRequest(serviceURL + COMPARE_SERVICE, "compare", properties);
        return parseCompareResponse(is);
    }

    public SummaryResponse summarize(CompressionMethod compressionMethod, float compressionRatio, String text)
    throws IOException, APIClientException {
        final Map<String,Object> properties = new HashMap<String,Object>();
        properties.put(ParamsValidator.func, compressionMethod.name());
        properties.put(ParamsValidator.compression_ratio, compressionRatio);
        properties.put(ParamsValidator.text, text);
        final InputStream is = sendRequest(serviceURL + SUMMARY_SERVICE, "summarize", properties);
        return parseSummaryResponse(is);
    }

    private InputStream sendRequest(String service, String group, Map<String,Object> properties)
    throws IOException {
        try {
            URL url = new URL(service);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setConnectTimeout(connTimeout);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);

            properties.put(ParamsValidator.app_id, appId);
            properties.put(ParamsValidator.app_key, appKey);
            StringBuilder data = ParamsValidator.getInstance().buildRequest(group, properties);
            httpURLConnection.addRequestProperty("Content-Length", Integer.toString(data.length()));

            log.info("sending data to ML API: " + data);

            DataOutputStream dataOutputStream = new DataOutputStream(
                    httpURLConnection.getOutputStream());
            dataOutputStream.write(data.toString().getBytes());
            dataOutputStream.close();

            if(httpURLConnection.getResponseCode() == 200) {
                return httpURLConnection.getInputStream();
            } else {
                return httpURLConnection.getErrorStream();
            }
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }

    private void checkError(JSONObject rootResponse) throws APIClientException {
        final JSONObject error = rootResponse.optJSONObject("error");
        if (error != null)
            try {
                throw new APIClientException(
                        String.format("An error occurred while invoking API. Message: [%s], code: [%d]",
                                error.getString("msg"),
                                error.getInt("code")
                        )
                );
            } catch (JSONException jsone) {
                throw new IllegalArgumentException("Invalid error message.", jsone);
            }
    }

    private GuessedLanguageResponse parseLangResponse(InputStream is) throws IOException, APIClientException {
        try {
            final JSONObject jsonResponse = new JSONObject(IOUtils.toString(is));
            checkError(jsonResponse);
            final JSONObject annotation = jsonResponse.getJSONObject("annotation");
            final String lang = annotation.getString("lang");
            final int cost = annotation.getInt("cost");
            return new GuessedLanguageResponse(lang, cost);
        } catch (JSONException jsone) {
            throw new APIClientException("An error occurred while parsing the lang service response.", jsone);
        }
    }

    private Clazz[] toClasses(JSONArray classes) throws JSONException {
        if (classes == null) return null;
        Clazz[] response = new Clazz[classes.length()];
        for (int i = 0; i < classes.length(); i++) {
            JSONObject clazzJSON = classes.getJSONObject(i);
            response[i] = new Clazz(
                    clazzJSON.getString("label"),
                    toURLOrFail(clazzJSON.getString("url")),
                    clazzJSON.getString("resource"),
                    clazzJSON.getDouble("prob")
            );
        }
        return response;
    }

    private Category[] toCategories(JSONArray categories) throws JSONException {
        if (categories == null) return null;
        Category[] response = new Category[categories.length()];
        for (int i = 0; i < categories.length(); i++) {
            JSONObject clazzJSON = categories.getJSONObject(i);
            response[i] = new Category(
                    clazzJSON.getString("label"),
                    toURLOrFail(clazzJSON.getString("url"))
            );
        }
        return response;
    }

    private External[] toExternals(JSONArray externals) throws JSONException {
        if (externals == null) return null;
        External[] response = new External[externals.length()];
        for (int i = 0; i < externals.length(); i++) {
            JSONObject clazzJSON = externals.getJSONObject(i);
            response[i] = new External(
                    clazzJSON.getString("label"),
                    toURLOrFail(clazzJSON.getString("url")),
                    clazzJSON.getString("resource")
            );
        }
        return response;
    }

    private Alt[] toAlts(JSONArray alts) throws JSONException {
        if (alts == null) return null;
        Alt[] response = new Alt[alts.length()];
        for (int i = 0; i < alts.length(); i++) {
            JSONObject clazzJSON = alts.getJSONObject(i);
            response[i] = new Alt(
                    clazzJSON.getString("form"),
                    clazzJSON.getDouble("freq")
            );
        }
        return response;
    }

    private Cross[] toCrosses(JSONArray crosses) throws JSONException {
        if (crosses == null) return null;
        Cross[] response = new Cross[crosses.length()];
        for (int i = 0; i < crosses.length(); i++) {
            JSONObject clazzJSON = crosses.getJSONObject(i);
            response[i] = new Cross(
                    clazzJSON.getString("lang"),
                    clazzJSON.getString("page")
            );
        }
        return response;
    }

    private NGram[] toNGrams(JSONArray ngramsArray) throws JSONException {
        JSONObject span;
        final NGram[] response = new NGram[ngramsArray.length()];
        for (int i = 0; i < ngramsArray.length(); i++) {
            span = ngramsArray.getJSONObject(i).getJSONObject("span");
            response[i] = new NGram(span.getInt("start"), span.getInt("end"));
        }
        return response;
    }

    private Image[] toImages(JSONArray images) throws JSONException {
        if (images == null) return null;
        Image[] response = new Image[images.length()];
        for (int i = 0; i < images.length(); i++) {
            JSONObject clazzJSON = images.getJSONObject(i);
            response[i] = new Image(
                    toURLOrFail(clazzJSON.getString("image")),
                    toURLOrFail(clazzJSON.getString("thumb"))
            );
        }
        return response;
    }

    private JSONObject parseAndCheckErrors(InputStream is) throws IOException, APIClientException {
        final JSONObject jsonResponse;
        try {
            jsonResponse = new JSONObject(IOUtils.toString(is));
        } catch (JSONException jsone) {
            throw new APIClientException("An error occurred while parsing the API response.", jsone);
        }
        checkError(jsonResponse);
        return jsonResponse;
    }

    private AnnotationResponse parseAnnotateResponse(InputStream is) throws IOException, APIClientException {
        try {
            final JSONObject jsonResponse = parseAndCheckErrors(is);
            final JSONObject annotation = jsonResponse.getJSONObject("annotation");
            final String lang = annotation.getString("lang");
            final int cost = annotation.getInt("cost");
            JSONArray keywordsJSON = annotation.getJSONArray("keyword");
            JSONObject keywordJSON, sense;
            Keyword keyword;
            Keyword[] keywords = new Keyword[keywordsJSON.length()];
            for(int i = 0; i < keywordsJSON.length(); i++) {
                keywordJSON = keywordsJSON.getJSONObject(i);
                sense = keywordJSON.optJSONObject("sense");
                keyword = new Keyword(
                        keywordJSON.getString("form"),
                        (float) keywordJSON.getDouble("rel"),
                        sense != null ? sense.getString("page") : null,
                        sense != null ? (float) sense.getDouble("prob") : 0,
                        keywordJSON.optString("abstract"),
                        toClasses(keywordJSON.optJSONArray("class")),
                        toCategories(keywordJSON.optJSONArray("category")),
                        toExternals(keywordJSON.optJSONArray("external")),
                        toAlts(keywordJSON.optJSONArray("alt")),
                        toCrosses(keywordJSON.optJSONArray("cross")),
                        toNGrams(keywordJSON.getJSONArray("ngram")),
                        toImages(keywordJSON.optJSONArray("image"))
                );
                keywords[i] = keyword;
            }
            return new AnnotationResponse(lang, keywords, cost);
        } catch (JSONException jsone) {
            throw new APIClientException("An error occurred while parsing the /annotate JSON data.", jsone);
        }
    }

    private CompareResponse parseCompareResponse(InputStream is) throws APIClientException, IOException {
        try {
            final JSONObject jsonResponse = parseAndCheckErrors(is);
            final JSONObject annotation = jsonResponse.getJSONObject("annotation");
            final int cost = annotation.getInt("cost");
            final float similarity = (float) annotation.getDouble("value");
            return new CompareResponse(cost, similarity);
        } catch (JSONException jsone) {
            throw new APIClientException("An error occurred while parsing the /compare JSON data.", jsone);
        }
    }

    private SummaryResponse parseSummaryResponse(InputStream is) throws APIClientException, IOException {
        try {
            final JSONObject jsonResponse = parseAndCheckErrors(is);
            final JSONObject annotation = jsonResponse.getJSONObject("annotation");
            final int cost = annotation.getInt("cost");
            JSONArray summariesJSON = annotation.getJSONArray("summary");
            final Summary[] summaries = new Summary[summariesJSON.length()];
            for(int i = 0; i < summariesJSON.length(); i++) {
                JSONObject summaryJSON = summariesJSON.getJSONObject(i);
                summaries[i] = new Summary(
                        summaryJSON.getString("sentence"),
                        (float) summaryJSON.getDouble("weight"),
                        summaryJSON.getInt("start"),
                        summaryJSON.getInt("end")
                );
            }
            return new SummaryResponse(cost, summaries);
        } catch (JSONException jsone) {
            throw new APIClientException("An error occurred while parsing the /compare JSON data.", jsone);
        }
    }

    private URL toURLOrFail(String urlTxt) {
        try {
            return new URL(urlTxt);
        } catch (MalformedURLException e) {
            throw new IllegalStateException(
                    String.format("Invalid value returned from API. Expected URL found [%s]", urlTxt)
            );
        }
    }

}
