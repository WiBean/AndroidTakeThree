package com.jmnow.wibeantakethree.brewingprograms.data;

import android.net.Uri;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.json.JSONObject;

import java.net.URI;
import java.util.List;

/**
 * Represents a single brewing program.
 */

public class BrewingProgram {
    public static final Integer NUMONOFFTIMES = 5;
    private Integer[] onTimes = new Integer[NUMONOFFTIMES];
    private Integer[] offTimes = new Integer[NUMONOFFTIMES];
    public static final Integer MIN_TIME_UNITS = 0;
    public static final Integer MAX_TIME_UNITS = 100;
    public static final String GOOGLE_SHORTEN_URL = "https://www.googleapis.com/urlshortener/v1/url";
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    // httpClient, make one to save resources
    private final OkHttpClient mHttpClient = new OkHttpClient();
    private String mId = "";
    private String mName = "";
    private String mDescription = "";
    private String mCreatedAt = "";
    private String mModifiedAt = "";
    private String mShortUrl = "";

    public BrewingProgram(CharSequence id, CharSequence name) {
        this.mId = id.toString();
        this.mName = name.toString();
        this.mDescription = "";
        for (Integer k = 0; k < NUMONOFFTIMES; ++k) {
            onTimes[k] = 0;
            offTimes[k] = 0;
        }
    }

    public BrewingProgram(CharSequence id, CharSequence name, CharSequence description, Integer[] onTimes, Integer[] offTimes) {
        this.mId = id.toString();
        this.mName = name.toString();
        this.mDescription = description.toString();
        this.onTimes = onTimes;
        this.offTimes = offTimes;
    }

    public static BrewingProgram fromUri(Uri androidUri) {
        BrewingProgram newGuy = new BrewingProgram("", "");
        try {
            List<NameValuePair> params = URLEncodedUtils.parse(new java.net.URI(androidUri.toString()), "UTF-8");
            for (int k = 0; k < params.size(); ++k) {
                newGuy.parseNameValuePair(params.get(k));
                System.out.println("key: " + params.get(k).getName() + " value: " + params.get(k).getValue());
            }
        } catch (Exception e) {
            // crap
        }
        return newGuy;
    }

    public String getCreatedAt() {
        return mCreatedAt;
    }

    public void setCreatedAt(String createdAt) {
        this.mCreatedAt = createdAt;
    }

    public String getModifiedAt() {
        return mModifiedAt;
    }

    public void setModifiedAt(String modifiedAt) {
        this.mModifiedAt = modifiedAt;
    }

    public String getId() {
        return this.mId;
    }

    public String getName() {
        return this.mName;
    }

    public String getDescription() {
        return this.mDescription;
    }

    public Integer[] getOnTimes() {
        return this.onTimes;
    }

    public Integer[] getOffTimes() {
        return this.offTimes;
    }

    public boolean setId(String id) {
        this.mId = id;
        return true;
    }

    public boolean setName(CharSequence name) {
        this.mName = name.toString();
        return true;
    }

    public boolean setDescription(CharSequence description) {
        this.mDescription = description.toString();
        return true;
    }

    public boolean setOnTimes(Integer[] onTimes) {
        if (onTimes.length > BrewingProgram.NUMONOFFTIMES) {
            return false;
        }
        int k = 0; //counter
        // make sure the times they gave are good
        for (; k < onTimes.length; k++) {
            if (onTimes[k] < 0) {
                return false;
            }
        }
        // fill the rest
        for (; k < BrewingProgram.NUMONOFFTIMES; k++) {
            onTimes[k] = 0;
        }
        this.onTimes = onTimes;
        return true;
    }

    public boolean setOffTimes(Integer[] offTimes) {
        if (offTimes.length > BrewingProgram.NUMONOFFTIMES) {
            return false;
        }
        int k = 0; //counter
        // make sure the times they gave are good
        for (; k < offTimes.length; k++) {
            if (offTimes[k] < 0) {
                return false;
            }
        }
        // fill the rest
        for (; k < BrewingProgram.NUMONOFFTIMES; k++) {
            offTimes[k] = 0;
        }
        this.offTimes = offTimes;
        return true;
    }

    public String getShortUrl() {
        return mShortUrl;
    }

    public void setShortUrl(String shortUrl) {
        this.mShortUrl = shortUrl;
    }

    public boolean shortenUrl() {
        final String bodyString = "{\"longUrl\": \"" + this.toUri().toString() + "\"}";
        RequestBody body = RequestBody.create(JSON, bodyString);
        Request request = new Request.Builder()
                .url(GOOGLE_SHORTEN_URL)
                .post(body)
                .build();
        try {
            Response response = mHttpClient.newCall(request).execute();
            // remove the newlines because there won't be any in this response and it messes up
            // the Java JSONObject parser
            final JSONObject bodyAsObject = new JSONObject(response.body().string().trim().replace("\n", ""));
            if ((response.code() == 200) &&
                    bodyAsObject.has("kind") &&
                    bodyAsObject.getString("kind").startsWith("urlshortener#url")) {
                this.setShortUrl(bodyAsObject.getString("id"));
                return true;
            }
            // if we get here, something was wrong
            System.out.println("bad response: " + response.body().string());
        } catch (Exception e) {
            System.out.println("getShortUrl Failed: " + e.getMessage() + ' ' + e.getClass());
        }
        return false;
    }

    @Override
    public String toString() {
        return mName + mDescription;
    }

    public URI toUri() {
        StringBuilder queryString = new StringBuilder();
        queryString.append("name=").append(mName).append("&");
        queryString.append("description=").append(mDescription).append("&");
        queryString.append("modified_at=").append(mModifiedAt).append("&");
        queryString.append("created_at=").append(mCreatedAt).append("&");
        for (int k = 0; k < NUMONOFFTIMES; ++k) {
            queryString.append("onF[").append(k).append("]=").append(onTimes[k]).append("&");
            queryString.append("offF[").append(k).append("]=").append(offTimes[k]).append("&");
        }
        try {
            return new URI("http", "www.wibean.com", "/brewingProgram/v1", queryString.toString(), null);
        } catch (Exception e) {
            System.out.println("BrewingProgram::toUri failed: " + e.getLocalizedMessage());
            return null;
        }
    }

    private void parseNameValuePair(NameValuePair thePair) {
        final String name = thePair.getName();
        switch (name) {
            case "name":
                this.setName(thePair.getValue());
                break;
            case "description":
                this.setDescription(thePair.getValue());
                break;
            case "created_at":
                this.setCreatedAt(thePair.getValue());
                break;
            default:
                // this version of the program only supports max 5 times, so multi-digit indecies are bad
                if (name.startsWith("onF[") && (name.length() == 6)) {
                    Integer index = Integer.valueOf(name.substring(4, 5));
                    if ((index > NUMONOFFTIMES) || (index < 0)) {
                        break;
                    }
                    Integer value = Integer.valueOf(thePair.getValue());
                    if ((value < MIN_TIME_UNITS) || (value > MAX_TIME_UNITS)) {
                        break;
                    }
                    onTimes[index] = value;
                } else if (name.startsWith("offF[") && (name.length() == 7)) {
                    Integer index = Integer.valueOf(name.substring(5, 6));
                    if ((index > NUMONOFFTIMES) || (index < 0)) {
                        break;
                    }
                    Integer value = Integer.valueOf(thePair.getValue());
                    if ((value < MIN_TIME_UNITS) || (value > MAX_TIME_UNITS)) {
                        break;
                    }
                    offTimes[index] = value;
                }
        }

    }
}
