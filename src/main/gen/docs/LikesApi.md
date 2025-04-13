# LikesApi

All URIs are relative to *http://localhost:8080*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**likesPost**](LikesApi.md#likesPost) | **POST** /likes | Save a liked match |
| [**likesSeekerIdGet**](LikesApi.md#likesSeekerIdGet) | **GET** /likes/{seekerId} | Get all liked matches by seeker |


<a name="likesPost"></a>
# **likesPost**
> Match likesPost(match)

Save a liked match

Saves a match that the seeker has liked.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LikesApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    LikesApi apiInstance = new LikesApi(defaultClient);
    Match match = new Match(); // Match | 
    try {
      Match result = apiInstance.likesPost(match);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling LikesApi#likesPost");
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
| **match** | [**Match**](Match.md)|  | |

### Return type

[**Match**](Match.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **201** | Match successfully liked |  -  |
| **400** | Error creating liked match |  -  |
| **500** | Internal server error |  -  |

<a name="likesSeekerIdGet"></a>
# **likesSeekerIdGet**
> List&lt;Match&gt; likesSeekerIdGet(seekerId)

Get all liked matches by seeker

Retrieves all matches that a specific seeker has liked.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LikesApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    LikesApi apiInstance = new LikesApi(defaultClient);
    String seekerId = "seekerId_example"; // String | The ID of the seeker (roommate user)
    try {
      List<Match> result = apiInstance.likesSeekerIdGet(seekerId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling LikesApi#likesSeekerIdGet");
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
| **seekerId** | **String**| The ID of the seeker (roommate user) | |

### Return type

[**List&lt;Match&gt;**](Match.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Liked matches found |  -  |
| **404** | No liked matches found |  -  |
| **500** | Internal server error |  -  |

