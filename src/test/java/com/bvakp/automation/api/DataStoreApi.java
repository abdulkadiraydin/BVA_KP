package com.bvakp.automation.api;

import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.options.RequestOptions;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class DataStoreApi {

    private final APIRequestContext apiRequestContext;

    public DataStoreApi(APIRequestContext apiRequestContext) {
        this.apiRequestContext = apiRequestContext;
    }

    public APIResponse getRecords(String apiUrl, String resourceId, int limit) {
        return apiRequestContext.get(
                apiUrl,
                RequestOptions.create()
                        .setQueryParam("resource_id", resourceId)
                        .setQueryParam("limit", limit)
        );
    }
    public int getStatusCode(APIResponse response) {
        return response.status();
    }

    public boolean isDataStoreApiSuccessful(APIResponse response) {
        return response.ok();
    }
    public String getResponseBody(APIResponse response) {
        return response.text();
    }
    public boolean isApiSuccess(APIResponse response) {
        JsonObject json = JsonParser.parseString(response.text()).getAsJsonObject();

        return json.get("success").getAsBoolean();
    }

    public int getRecordCount(APIResponse response) {
        JsonObject json = JsonParser.parseString(response.text()).getAsJsonObject();

        return json.getAsJsonObject("result")
                .getAsJsonArray("records")
                .size();
    }
}