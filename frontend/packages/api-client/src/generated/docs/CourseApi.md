# CourseApi

All URIs are relative to *http://localhost:8080*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**addSession**](#addsession) | **POST** /api/v1/courses/{id}/sessions | 회차 추가|
|[**closeCourse**](#closecourse) | **POST** /api/v1/courses/{id}/close | 과정 종료|
|[**createCourse**](#createcourse) | **POST** /api/v1/courses | 과정 등록|
|[**getCourse**](#getcourse) | **GET** /api/v1/courses/{id} | 과정 상세 조회|
|[**getCourses**](#getcourses) | **GET** /api/v1/courses | 과정 목록 조회|
|[**updateCourse**](#updatecourse) | **PUT** /api/v1/courses/{id} | 과정 수정|

# **addSession**
> SessionResponse addSession(sessionRequest)


### Example

```typescript
import {
    CourseApi,
    Configuration,
    SessionRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new CourseApi(configuration);

let id: number; // (default to undefined)
let sessionRequest: SessionRequest; //

const { status, data } = await apiInstance.addSession(
    id,
    sessionRequest
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **sessionRequest** | **SessionRequest**|  | |
| **id** | [**number**] |  | defaults to undefined|


### Return type

**SessionResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**201** | 추가됨 |  -  |
|**409** | 종료된 과정 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **closeCourse**
> CourseDetailResponse closeCourse()


### Example

```typescript
import {
    CourseApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new CourseApi(configuration);

let id: number; // (default to undefined)

const { status, data } = await apiInstance.closeCourse(
    id
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **id** | [**number**] |  | defaults to undefined|


### Return type

**CourseDetailResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 종료됨 |  -  |
|**404** | 과정이 없음 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **createCourse**
> CourseDetailResponse createCourse(courseRequest)


### Example

```typescript
import {
    CourseApi,
    Configuration,
    CourseRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new CourseApi(configuration);

let courseRequest: CourseRequest; //

const { status, data } = await apiInstance.createCourse(
    courseRequest
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **courseRequest** | **CourseRequest**|  | |


### Return type

**CourseDetailResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**201** | 등록됨 |  -  |
|**400** | 입력값 오류 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getCourse**
> CourseDetailResponse getCourse()


### Example

```typescript
import {
    CourseApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new CourseApi(configuration);

let id: number; // (default to undefined)

const { status, data } = await apiInstance.getCourse(
    id
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **id** | [**number**] |  | defaults to undefined|


### Return type

**CourseDetailResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 조회 성공 |  -  |
|**404** | 과정이 없음 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getCourses**
> PageResponseCourseSummaryResponse getCourses()


### Example

```typescript
import {
    CourseApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new CourseApi(configuration);

let keyword: string; // (optional) (default to undefined)
let category: string; // (optional) (default to undefined)
let page: number; // (optional) (default to 0)
let size: number; // (optional) (default to 10)
let sort: Array<string>; // (optional) (default to undefined)

const { status, data } = await apiInstance.getCourses(
    keyword,
    category,
    page,
    size,
    sort
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **keyword** | [**string**] |  | (optional) defaults to undefined|
| **category** | [**string**] |  | (optional) defaults to undefined|
| **page** | [**number**] |  | (optional) defaults to 0|
| **size** | [**number**] |  | (optional) defaults to 10|
| **sort** | **Array&lt;string&gt;** |  | (optional) defaults to undefined|


### Return type

**PageResponseCourseSummaryResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **updateCourse**
> CourseDetailResponse updateCourse(courseRequest)


### Example

```typescript
import {
    CourseApi,
    Configuration,
    CourseRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new CourseApi(configuration);

let id: number; // (default to undefined)
let courseRequest: CourseRequest; //

const { status, data } = await apiInstance.updateCourse(
    id,
    courseRequest
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **courseRequest** | **CourseRequest**|  | |
| **id** | [**number**] |  | defaults to undefined|


### Return type

**CourseDetailResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 수정됨 |  -  |
|**404** | 과정이 없음 |  -  |
|**409** | 종료된 과정 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

