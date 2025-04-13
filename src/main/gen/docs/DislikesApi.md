# DislikesApi

All URIs are relative to *http://localhost:8080*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**dislikesPost**](DislikesApi.md#dislikesPost) | **POST** /dislikes | Save a disliked match |
| [**dislikesSeekerIdGet**](DislikesApi.md#dislikesSeekerIdGet) | **GET** /dislikes/{seekerId} | Get all disliked matches by seeker |


<a name="dislikesPost"></a>
# **dislikesPost**
> Dislike dislikesPost(dislike)

Save a disliked match

Saves a match that the seeker has disliked.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.DislikesApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    DislikesApi apiInstance = new DislikesApi(defaultClient);
    Dislike dislike = new Dislike(); // Dislike | 
    try {
      Dislike result = apiInstance.dislikesPost(dislike);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling DislikesApi#dislikesPost");
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
| **dislike** | [**Dislike**](Dislike.md)|  | |

### Return type

[**Dislike**](Dislike.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **201** | Match successfully marked as disliked |  -  |
| **400** | Error creating disliked match |  -  |
| **500** | Internal server error |  -  |

<a name="dislikesSeekerIdGet"></a>
# **dislikesSeekerIdGet**
> List&lt;Dislike&gt; dislikesSeekerIdGet(seekerId)

Get all disliked matches by seeker

Retrieves all matches that a specific seeker has disliked.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.DislikesApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    DislikesApi apiInstance = new DislikesApi(defaultClient);
    String seekerId = "seekerId_example"; // String | The ID of the seeker (roommate user)
    try {
      List<Dislike> result = apiInstance.dislikesSeekerIdGet(seekerId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling DislikesApi#dislikesSeekerIdGet");
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

[**List&lt;Dislike&gt;**](Dislike.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Disliked matches found |  -  |
| **404** | No disliked matches found |  -  |
| **500** | Internal server error |  -  |

