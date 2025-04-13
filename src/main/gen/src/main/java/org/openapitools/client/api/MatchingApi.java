/*
 * RooMatch API
 * API documentation for RooMatch backend
 *
 * The version of the OpenAPI document: 1.0.0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


package org.openapitools.client.api;

import org.openapitools.client.ApiCallback;
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.ApiResponse;
import org.openapitools.client.Configuration;
import org.openapitools.client.Pair;
import org.openapitools.client.ProgressRequestBody;
import org.openapitools.client.ProgressResponseBody;

import com.google.gson.reflect.TypeToken;

import java.io.IOException;


import org.openapitools.client.model.Match;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.GenericType;

public class MatchingApi {
    private ApiClient localVarApiClient;
    private int localHostIndex;
    private String localCustomBaseUrl;

    public MatchingApi() {
        this(Configuration.getDefaultApiClient());
    }

    public MatchingApi(ApiClient apiClient) {
        this.localVarApiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return localVarApiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.localVarApiClient = apiClient;
    }

    public int getHostIndex() {
        return localHostIndex;
    }

    public void setHostIndex(int hostIndex) {
        this.localHostIndex = hostIndex;
    }

    public String getCustomBaseUrl() {
        return localCustomBaseUrl;
    }

    public void setCustomBaseUrl(String customBaseUrl) {
        this.localCustomBaseUrl = customBaseUrl;
    }

    /**
     * Build call for matchSeekerIdGet
     * @param seekerId The ID of the roommate seeker (required)
     * @param _callback Callback for upload/download progress
     * @return Call to execute
     * @throws ApiException If fail to serialize the request body object
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> A single match returned </td><td>  -  </td></tr>
        <tr><td> 204 </td><td> No more matches </td><td>  -  </td></tr>
        <tr><td> 400 </td><td> Bad request (missing seekerId) </td><td>  -  </td></tr>
     </table>
     */
    public okhttp3.Call matchSeekerIdGetCall(String seekerId, final ApiCallback _callback) throws ApiException {
        String basePath = null;
        // Operation Servers
        String[] localBasePaths = new String[] {  };

        // Determine Base Path to Use
        if (localCustomBaseUrl != null){
            basePath = localCustomBaseUrl;
        } else if ( localBasePaths.length > 0 ) {
            basePath = localBasePaths[localHostIndex];
        } else {
            basePath = null;
        }

        Object localVarPostBody = null;

        // create path and map variables
        String localVarPath = "/match/{seekerId}"
            .replaceAll("\\{" + "seekerId" + "\\}", localVarApiClient.escapeString(seekerId.toString()));

        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
        Map<String, String> localVarHeaderParams = new HashMap<String, String>();
        Map<String, String> localVarCookieParams = new HashMap<String, String>();
        Map<String, Object> localVarFormParams = new HashMap<String, Object>();

        final String[] localVarAccepts = {
            "application/json"
        };
        final String localVarAccept = localVarApiClient.selectHeaderAccept(localVarAccepts);
        if (localVarAccept != null) {
            localVarHeaderParams.put("Accept", localVarAccept);
        }

        final String[] localVarContentTypes = {
            
        };
        final String localVarContentType = localVarApiClient.selectHeaderContentType(localVarContentTypes);
        if (localVarContentType != null) {
            localVarHeaderParams.put("Content-Type", localVarContentType);
        }

        String[] localVarAuthNames = new String[] {  };
        return localVarApiClient.buildCall(basePath, localVarPath, "GET", localVarQueryParams, localVarCollectionQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAuthNames, _callback);
    }

    @SuppressWarnings("rawtypes")
    private okhttp3.Call matchSeekerIdGetValidateBeforeCall(String seekerId, final ApiCallback _callback) throws ApiException {
        
        // verify the required parameter 'seekerId' is set
        if (seekerId == null) {
            throw new ApiException("Missing the required parameter 'seekerId' when calling matchSeekerIdGet(Async)");
        }
        

        okhttp3.Call localVarCall = matchSeekerIdGetCall(seekerId, _callback);
        return localVarCall;

    }

    /**
     * Get next match for seeker
     * Returns a single match for the seeker.
     * @param seekerId The ID of the roommate seeker (required)
     * @return Match
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> A single match returned </td><td>  -  </td></tr>
        <tr><td> 204 </td><td> No more matches </td><td>  -  </td></tr>
        <tr><td> 400 </td><td> Bad request (missing seekerId) </td><td>  -  </td></tr>
     </table>
     */
    public Match matchSeekerIdGet(String seekerId) throws ApiException {
        ApiResponse<Match> localVarResp = matchSeekerIdGetWithHttpInfo(seekerId);
        return localVarResp.getData();
    }

    /**
     * Get next match for seeker
     * Returns a single match for the seeker.
     * @param seekerId The ID of the roommate seeker (required)
     * @return ApiResponse&lt;Match&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> A single match returned </td><td>  -  </td></tr>
        <tr><td> 204 </td><td> No more matches </td><td>  -  </td></tr>
        <tr><td> 400 </td><td> Bad request (missing seekerId) </td><td>  -  </td></tr>
     </table>
     */
    public ApiResponse<Match> matchSeekerIdGetWithHttpInfo(String seekerId) throws ApiException {
        okhttp3.Call localVarCall = matchSeekerIdGetValidateBeforeCall(seekerId, null);
        Type localVarReturnType = new TypeToken<Match>(){}.getType();
        return localVarApiClient.execute(localVarCall, localVarReturnType);
    }

    /**
     * Get next match for seeker (asynchronously)
     * Returns a single match for the seeker.
     * @param seekerId The ID of the roommate seeker (required)
     * @param _callback The callback to be executed when the API call finishes
     * @return The request call
     * @throws ApiException If fail to process the API call, e.g. serializing the request body object
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> A single match returned </td><td>  -  </td></tr>
        <tr><td> 204 </td><td> No more matches </td><td>  -  </td></tr>
        <tr><td> 400 </td><td> Bad request (missing seekerId) </td><td>  -  </td></tr>
     </table>
     */
    public okhttp3.Call matchSeekerIdGetAsync(String seekerId, final ApiCallback<Match> _callback) throws ApiException {

        okhttp3.Call localVarCall = matchSeekerIdGetValidateBeforeCall(seekerId, _callback);
        Type localVarReturnType = new TypeToken<Match>(){}.getType();
        localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
        return localVarCall;
    }
}
