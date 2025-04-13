# PropertiesApi

All URIs are relative to *http://localhost:8080*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**propertiesOwnerIdGet**](PropertiesApi.md#propertiesOwnerIdGet) | **GET** /properties/{ownerId} | Get all properties of a specific owner |
| [**propertiesOwnerIdPost**](PropertiesApi.md#propertiesOwnerIdPost) | **POST** /properties/{ownerId} | Upload a new property |


<a name="propertiesOwnerIdGet"></a>
# **propertiesOwnerIdGet**
> List&lt;Property&gt; propertiesOwnerIdGet(ownerId)

Get all properties of a specific owner

Returns all properties associated with a property owner

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.PropertiesApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    PropertiesApi apiInstance = new PropertiesApi(defaultClient);
    String ownerId = "ownerId_example"; // String | The ID of the property owner
    try {
      List<Property> result = apiInstance.propertiesOwnerIdGet(ownerId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling PropertiesApi#propertiesOwnerIdGet");
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
| **ownerId** | **String**| The ID of the property owner | |

### Return type

[**List&lt;Property&gt;**](Property.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Properties found |  -  |
| **404** | No properties found |  -  |
| **500** | Internal server error |  -  |

<a name="propertiesOwnerIdPost"></a>
# **propertiesOwnerIdPost**
> PropertyUploadResponse propertiesOwnerIdPost(ownerId, property)

Upload a new property

Adds a new property associated with an owner

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.PropertiesApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    PropertiesApi apiInstance = new PropertiesApi(defaultClient);
    String ownerId = "ownerId_example"; // String | The ID of the property owner
    Property property = new Property(); // Property | 
    try {
      PropertyUploadResponse result = apiInstance.propertiesOwnerIdPost(ownerId, property);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling PropertiesApi#propertiesOwnerIdPost");
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
| **ownerId** | **String**| The ID of the property owner | |
| **property** | [**Property**](Property.md)|  | |

### Return type

[**PropertyUploadResponse**](PropertyUploadResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **201** | Property successfully added |  -  |
| **400** | Error creating property |  -  |
| **500** | Internal server error |  -  |

