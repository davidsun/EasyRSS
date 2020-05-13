0.7.6 2020-05-28 by [@Alkarex](https://github.com/Alkarex)
- Fix regression: crash when clicking "Original page"
- Allow HTTP (in addition of HTTPS)
- Fix logout

0.7.5-dev 2020-04-25 by [@Alkarex](https://github.com/Alkarex)
- Use published time instead of crawl time from API to show article publication date

0.7.4 2020-04-19 by [@Alkarex](https://github.com/Alkarex)
- Fix warnings for F-Droid

0.7.2 2020-04-15 by [@Alkarex](https://github.com/Alkarex)
- Stable release

0.7.0 2020-04-14 by [@Alkarex](https://github.com/Alkarex)
- Update IDE to Android Studio 3.6.2 (IntelliJ) with Gradle
- Update source code for compatibility Android 10.0 Q, API 29 (retaining compatibility with Android 2.1 Eclair, API 7)
- Move local files to internal storage, as external storage code is not compatible with Android 6+
- (Temporarily) disable *New items notification*, which was not compatible with Android 6+
- i18n: [pt-BR](https://github.com/Alkarex/EasyRSS/pull/42)

0.6.08 2017-07-22 by [@Alkarex](https://github.com/Alkarex)
- Bumped version number for F-Droid.

0.6.06 2015-05-24 by [@Alkarex](https://github.com/Alkarex)
- Renamed package and namespace to org.freshrss.easyrss. More cleaning for F-Droid.

0.6.05 2015-05-03 by [@Alkarex](https://github.com/Alkarex)
- Cleaned dependencies/libraries and replaced the remaining ones with their source code, for submission to F-Droid.

0.6.04 2015-05-02 by [@Alkarex](https://github.com/Alkarex)
- Added support for HTTPS TLSv1.1 and TLSv1.2 for Android 4.1+

0.6.03 2014-03-07 by [@Alkarex](https://github.com/Alkarex)
- Added French translation
- Improved Danish translation

0.6.02 2014-03-05 by [@Alkarex](https://github.com/Alkarex)
- Avoid encoding slashes as `%2F` for better compatibility with [Apache](http://httpd.apache.org/docs/trunk/mod/core.html#allowencodedslashes) and IIS.
- Minor bug corrections

0.6.01 2014-03-01 by [@Alkarex](https://github.com/Alkarex)
- Customizable URL for choosing an alternative RSS service compatible with Google Reader API such as FreshRSS http://freshrss.org
- Removed Google Analytics
- Removed permissions GET_ACCOUNTS, USE_CREDENTIALS, MANAGE_ACCOUNTS, RECEIVE_BOOT_COMPLETED

0.5.16 Open Source
- Disable GPU acceleration support on Android 4.1 devises (for it may cause native crash).
- Added Danish translation

0.5.15
- Enabling GPU acceleration support on Android 4.1/4.2 devices.
- UI updates for popup windows.
- Performance improvements when switching between items.

0.5.14
- Fix re-start of the app when orientation changes.

0.5.13
- Fix bug in syncing (unable to sync sometimes).
- GPU acceleration support.

0.5.12
- Spanish support.
- Be able to save image to SD Card.
- Faster scrolling in viewing items.
- Fix bug in syncing when sync failed.

0.5.11
- Be able to change syncing interval.
- Fix several bugs in syncing.

0.5.10
- Faster "mark all as read".
- Be able to copy item to clipboard.
- Syncing plan optimization.
- Auto-hide item menu.

0.5.9
- Be able to choose between built-in and external browsers.
- UI speed up.

0.5.8
- Built-in browser.
- UI speed up.

0.5.7
- Scrolling improvements.
- Reducing view switching waiting time.

0.5.6
- Setting to turn off mark all as read confirmation.
- UI updates.

0.5.5
- In-app image viewing.
- UI performance improvements.
- Fix a bug that may cause crash when syncing.

0.5.4
- Fix crash bug on some Android devises.

0.5.3
- UI improvements, more fluent scrolling and improved menu bar in item reading interface.
- Faster "Mark all as read".
- Showing "1000+" instead of "1000" when there're more than 1000 items.
- Fix a UI issue on ICS machines.

0.5.2
- Dark theme support.
- More sharing options (support sending item content to Google+, etc.)
- Larger font support.

0.5.1
- Turn back to home screen after "mark all as read".
- Fix a bug in "mark previous as read".

0.5.0
- (IMPORTANT) UI update.
- Fix a bug that may cause crash in syncing.

0.4.3
- Fix a bug in syncing.
- Fix a bug that may cause crash in some cases.

0.4.2
- Notification.
- UI improvements (greatly optimized when swiping back).

0.4.1
- Item ordering (ascending or descending ordering of publishing time).
- Several bugfixes.

0.4.0
- (IMPORTANT) EasyRSS Mobilizer.
- Fix a bug in background syncing.

0.3.3
- UI improvements (faster speed).
- Support for smaller font size.
- Fix a bug in syncing item state.
- Fix a bug in fetching article link.
- Fix a bug in showing the author of the article.

0.3.2
- (IMPORTANT) UI improvements (less memory & faster speed).
- Fix a bug in syncing unread items in subscriptions.
- Sharing improvements.

0.3.1
- Mark as unread feature.
- Fix a bug that may cause large memory consumption.
- Optimize scrolling when viewing items.
- Syncing optimization.

0.3.0
- (IMPORTANT) Swipe to turn back to the last screen.
- Sharing improvements.
- Syncing optimization.

0.2.8
- Fix encoding bugs on Meizu phones.
  (Be sure that this bugfix will be valid only for the newer-loaded items)
- Larger font support.
- Volume key navigation between items.
- Item syncing optimization.

0.2.7
- Item auto loading when reading.
- Fixed a bug in sharing items.
- Fixed a bug in syncing.

0.2.5
- Support sharing with subjects.

0.2.4
- Optimization in syncing.
- Support for Amazon app store.

0.2.3:
- More choices in image syncing.

0.2.2:
- Add Japanese support.
- Fixed a bug in syncing.

0.2.1:
- Better "send item content to" feature.

0.2.0:
- Adapt to new Google Reader.
- Add "send item content to" feature.
