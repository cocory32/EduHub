# SessionResponse


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **number** |  | [optional] [default to undefined]
**startDate** | **string** |  | [optional] [default to undefined]
**place** | **string** |  | [optional] [default to undefined]
**capacity** | **number** |  | [optional] [default to undefined]
**remainingSeats** | **number** | 남은 자리. 수강 신청(3주차) 전까지는 정원과 같다 | [optional] [default to undefined]
**status** | **string** |  | [optional] [default to undefined]

## Example

```typescript
import { SessionResponse } from './api';

const instance: SessionResponse = {
    id,
    startDate,
    place,
    capacity,
    remainingSeats,
    status,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
