# PropertyOwnerApi

All URIs are relative to *http://localhost:8080*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**ownersRegisterPost**](PropertyOwnerApi.md#ownersRegisterPost) | **POST** /owners/register | Register a property owner user |


<a name="ownersRegisterPost"></a>
# **ownersRegisterPost**
> RegisterResponse ownersRegisterPost(propertyOwnerUser)

Register a property owner user

Creates a new property owner user account

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.PropertyOwnerApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    PropertyOwnerApi apiInstance = new PropertyOwnerApi(defaultClient);
    PropertyOwnerUser propertyOwnerUser = new PropertyOwnerUser(); // PropertyOwnerUser | 
    try {
      RegisterResponse result = apiInstance.ownersRegisterPost(propertyOwnerUser);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling PropertyOwnerApi#ownersRegisterPost");
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
| **propertyOwnerUser** | [**PropertyOwnerUser**](PropertyOwnerUser.md)|  | |

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
| **201** | Property owner registration successful |  -  |
| **400** | Error creating user |  -  |
| **500** | Internal server error |  -  |

