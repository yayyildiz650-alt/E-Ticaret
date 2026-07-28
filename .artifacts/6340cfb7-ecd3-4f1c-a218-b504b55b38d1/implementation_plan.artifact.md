# Implementation Plan - Fix SAPI Shutdown and Auth Issues

The application is experiencing a `Failed to shutdown SAPI; threads might leak` error, accompanied by `NETWORK_ERROR` and `DeviceManagementRequired`. This usually happens when the Vertex AI (Gemini) SDK fails to handle network interruptions or Google account compliance issues (MDM) gracefully.

## User Review Required

> [!IMPORTANT]
> The `DeviceManagementRequired` error suggests that the Google account being used on the device requires enterprise device management (MDM). If the user's account is a corporate/Workspace account, they may need to enroll their device or use a personal account. The app will be updated to provide a clearer error message for this case.

## Proposed Changes

### [Utils]

#### [NEW] [NetworkUtils.kt](file:///C:/Users/yayyi/AndroidStudioProjects/ETicaret/app/src/main/java/com/example/e_ticaret/Utils/NetworkUtils.kt)
- Create a utility to check for internet connectivity.

### [AiSystem]

#### [MODIFY] [AiViewModel.kt](file:///C:/Users/yayyi/AndroidStudioProjects/ETicaret/app/src/main/java/com/example/e_ticaret/AiSystem/AiViewModel.kt)
- Update `AiViewModel` to check for network connectivity before sending messages.
- Add robust error handling to catch specific Google Play Services / Auth exceptions.
- Provide user-friendly error messages for `DeviceManagementRequired` and `NO_NETWORK`.

#### [MODIFY] [AppNavigation.kt](file:///C:/Users/yayyi/AndroidStudioProjects/ETicaret/app/src/main/java/com/example/e_ticaret/AppNavigation.kt)
- Update the `AiViewModel` factory to pass the application context.

## Verification Plan

### Automated Tests
- I will verify the code compiles successfully using `gradle_build`.

### Manual Verification
- The user should test the AI chat functionality in the following scenarios:
    1. **No Internet**: Verify the app shows a "No internet connection" message instead of crashing or leaking threads.
    2. **Managed Account**: If possible, test with a managed account to see if the "Device Management Required" error is caught and reported clearly.
