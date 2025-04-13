# MatchingApi

All URIs are relative to *http://localhost:8080*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**matchSeekerIdGet**](MatchingApi.md#matchSeekerIdGet) | **GET** /match/{seekerId} | Get next match for seeker |


<a name="matchSeekerIdGet"></a>
# **matchSeekerIdGet**
> Match matchSeekerIdGet(seekerId)

Get next match for seeker

Returns a single match for the seeker.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.MatchingApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    MatchingApi apiInstance = new MatchingApi(defaultClient);
    String seekerId = "seekerId_example"; // String | The ID of the roommate seeker
    try {
      Match result = apiInstance.matchSeekerIdGet(seekerId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling MatchingApi#matchSeekerIdGet");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **seekerId** | **String**| The ID of the roommate seeker | |

### Return type

[**Match**](Match.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | A single match returned |  -  |
| **204** | No more matches |  -  |
| **400** | Bad request (missing seekerId) |  -  |

