# RoommateApi

All URIs are relative to *http://localhost:8080*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**roommatesGenerateBioPost**](RoommateApi.md#roommatesGenerateBioPost) | **POST** /roommates/generate-bio | Generate a personal bio for a roommate |
| [**roommatesRegisterPost**](RoommateApi.md#roommatesRegisterPost) | **POST** /roommates/register | Register a roommate user |
| [**roommatesSeekerIdGet**](RoommateApi.md#roommatesSeekerIdGet) | **GET** /roommates/{seekerId} | Get roommate by ID |


<a name="roommatesGenerateBioPost"></a>
# **roommatesGenerateBioPost**
> BioResponse roommatesGenerateBioPost(bioRequest)

Generate a personal bio for a roommate

Uses Gemini AI to generate a short personal bio based on user data.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.RoommateApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    RoommateApi apiInstance = new RoommateApi(defaultClient);
    BioRequest bioRequest = new BioRequest(); // BioRequest | 
    try {
      BioResponse result = apiInstance.roommatesGenerateBioPost(bioRequest);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling RoommateApi#roommatesGenerateBioPost");
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
| **bioRequest** | [**BioRequest**](BioRequest.md)|  | |

### Return type

[**BioResponse**](BioResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Generated personal bio |  -  |
| **400** | Bad request |  -  |
| **500** | Internal server error |  -  |

<a name="roommatesRegisterPost"></a>
# **roommatesRegisterPost**
> RegisterResponse roommatesRegisterPost(roommateUser)

Register a roommate user

Creates a new roommate user account

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.RoommateApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    RoommateApi apiInstance = new RoommateApi(defaultClient);
    RoommateUser roommateUser = new RoommateUser(); // RoommateUser | 
    try {
      RegisterResponse result = apiInstance.roommatesRegisterPost(roommateUser);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling RoommateApi#roommatesRegisterPost");
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
| **roommateUser** | [**RoommateUser**](RoommateUser.md)|  | |

### Return type

[**RegisterResponse**](RegisterResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **201** | Roommate registration successful |  -  |
| **400** | Error creating user |  -  |
| **500** | Internal server error |  -  |

<a name="roommatesSeekerIdGet"></a>
# **roommatesSeekerIdGet**
> RoommateUser roommatesSeekerIdGet(seekerId)

Get roommate by ID

Returns a roommate user by ID

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.RoommateApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    RoommateApi apiInstance = new RoommateApi(defaultClient);
    String seekerId = "seekerId_example"; // String | The ID of the roommate user
    try {
      RoommateUser result = apiInstance.roommatesSeekerIdGet(seekerId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling RoommateApi#roommatesSeekerIdGet");
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
| **seekerId** | **String**| The ID of the roommate user | |

### Return type

[**RoommateUser**](RoommateUser.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Roommate found |  -  |
| **404** | Roommate not found |  -  |
| **500** | Internal server error |  -  |

