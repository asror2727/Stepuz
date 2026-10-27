STEPUZ — Android Studio source project (prototype)

What works:
- STEPUZ launch animation.
- Requests Android physical-activity permission.
- Requests notification permission on Android 13+.
- Offers Android's battery-optimization exemption screen.
- Uses the phone's real hardware step counter/detector sensor when available.
- Start/stop and reset buttons.

Important limitations:
- This is source code, not a prebuilt APK.
- Requires Android Studio / Android SDK to build.
- A phone must have a supported step sensor. It cannot guarantee a fixed number of steps after a particular distance; sensor readings vary by device and gait.
- Background step counting is NOT yet implemented as a foreground service. The battery settings prompt alone cannot guarantee counting while the app is closed. Do not treat this prototype as a production anti-cheat or money-tracking app.
- This prototype intentionally does not request location: GPS is not required for hardware step-counter sensor readings. If a later version needs GPS-based distance, request location separately with a clear explanation.
