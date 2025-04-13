

# RoommateUser

Model representing a roommate user who is looking for an apartment and roommates

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**fullName** | **String** |  |  |
|**phoneNumber** | **String** |  |  |
|**gender** | [**GenderEnum**](#GenderEnum) |  |  |
|**birthDate** | **String** |  |  |
|**work** | **String** |  |  |
|**attributes** | [**List&lt;AttributesEnum&gt;**](#List&lt;AttributesEnum&gt;) |  |  |
|**hobbies** | [**List&lt;HobbiesEnum&gt;**](#List&lt;HobbiesEnum&gt;) |  |  |
|**lookingForRoomies** | [**List&lt;LookingForRoomiesPreference&gt;**](LookingForRoomiesPreference.md) |  |  |
|**lookingForCondo** | [**List&lt;LookingForCondoPreference&gt;**](LookingForCondoPreference.md) |  |  |
|**roommatesNumber** | **Integer** |  |  |
|**minPropertySize** | **Integer** |  |  |
|**maxPropertySize** | **Integer** |  |  |
|**minPrice** | **Integer** |  |  |
|**maxPrice** | **Integer** |  |  |
|**email** | **String** |  |  |
|**password** | **String** |  |  |
|**profilePicture** | **String** |  |  |
|**personalBio** | **String** |  |  |



## Enum: GenderEnum

| Name | Value |
|---- | -----|
| MALE | &quot;MALE&quot; |
| FEMALE | &quot;FEMALE&quot; |
| OTHER | &quot;OTHER&quot; |



## Enum: List&lt;AttributesEnum&gt;

| Name | Value |
|---- | -----|
| SMOKER | &quot;SMOKER&quot; |
| STUDENT | &quot;STUDENT&quot; |
| PET_LOVER | &quot;PET_LOVER&quot; |
| PET_OWNER | &quot;PET_OWNER&quot; |
| VEGETARIAN | &quot;VEGETARIAN&quot; |
| CLEAN | &quot;CLEAN&quot; |
| NIGHT_WORKER | &quot;NIGHT_WORKER&quot; |
| IN_RELATIONSHIP | &quot;IN_RELATIONSHIP&quot; |
| KOSHER | &quot;KOSHER&quot; |
| JEWISH | &quot;JEWISH&quot; |
| MUSLIM | &quot;MUSLIM&quot; |
| CHRISTIAN | &quot;CHRISTIAN&quot; |
| REMOTE_WORKER | &quot;REMOTE_WORKER&quot; |
| ATHEIST | &quot;ATHEIST&quot; |
| QUIET | &quot;QUIET&quot; |



## Enum: List&lt;HobbiesEnum&gt;

| Name | Value |
|---- | -----|
| MUSICIAN | &quot;MUSICIAN&quot; |
| SPORT | &quot;SPORT&quot; |
| COOKER | &quot;COOKER&quot; |
| PARTY | &quot;PARTY&quot; |
| TV | &quot;TV&quot; |
| GAMER | &quot;GAMER&quot; |
| ARTIST | &quot;ARTIST&quot; |
| DANCER | &quot;DANCER&quot; |
| WRITER | &quot;WRITER&quot; |



