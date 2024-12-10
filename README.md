# SWENG888 - Final Project: Scope Zeroing App

## Repo Creators / Original Authors:

- Andrew Young
- Roberto Luna
- André Medeiros

## Dependencies:

- Java 17

## Setup Instructions & Build Process:

### Clone
- With ssh setup properly, run 'git clone git@github.com:andre91998/SWENG888_FinalProject.git' locally

### Environment Setup
- Open the project in Android Studio will automatically run a gradle sync and setup it up for you
- Follow the API Configuration section to configure your API KEYS

### Building

    - gradlew assembleDebug

## Project Overview

The Dialed-In Zeroing App is a comprehensive Android application focused on delivering key functionalities for tracking and managing scope zeroing settings. Its features span user authentication, scope management, real-time environmental data, and mapping capabilities. The app serves as a centralized platform that combines technical tools and intuitive design, catering to both novice and experienced shooters. 

Key system capabilities include:
- Recording and saving detailed scope zeroing data such as windage, elevation, distance, and location.
- Providing access to real-time weather information for shooting preparation.
- Enabling users to locate nearby shooting ranges dynamically and view markers on Google Maps.
- Allowing users to personalize their profiles and manage their saved data securely.
- Delivering a seamless user interface with responsive design and adherence to Material Design principles.

## API Configuration

### Firebase:
- Authentication for user login, signup, and session management.
- Firestore for user profile storage and dynamic scope list updates.
- Firebase Storage for managing profile images.
- Setup Process:
  - The application should run fine with the default API key. If for some reason you must setup a new key, follow the process [here](https://firebase.google.com/docs/projects/api-keys)

### Google Maps API:
- Display user locations and dynamically added markers for shooting ranges.
- Setup Process:
  - - The application should run fine with the default API key. If for some reason you must setup a new key, follow the process [here](https://developers.google.com/maps/documentation/embed/get-api-key)

### OpenWeather API:
- Fetch real-time weather conditions based on user input or location.
- Setup Process:
  - The application should run fine with the default API key. If for some reason you must setup a new key, follow the process [here](https://openweathermap.org/appid)

## App Architecture

The source code is structured into well-defined packages, ensuring clarity and maintainability:
```
com.sweng.scopehud
|__database
|__ui
|    gallery
|    home
|    slideshow
|__util
```
- scopehud: 
  - Contains all activity classes (MainActivity, LoginActivity, SettingsActivity, etc.) responsible for UI and user interaction.
- Util: 
  - Includes utility classes (Scope, ScopeZero, ScopeRecyclerViewAdapter) for data modeling and adapter logic.
- Database: 
  - Contains classes like DBHandler for managing SQLite database operations.
- Resources: 
  - Includes layout files (XML), themes, drawable assets, and string resources for a consistent UI.


## Usage Guide

The Dialed-In Zeroing App is designed to help users manage and optimize their shooting activities by allowing them to zero their scopes, access weather conditions, locate shooting ranges, and maintain a record of their settings and adjustments. This guide provides step-by-step instructions for each functionality, complete with screenshots and descriptions.

### Login/Signup
- Steps:
  - Open the app. The splash screen will load briefly.
  - If you have an account, enter your email and password on the login screen and tap Login.
  - To create a new account, tap Sign Up and fill in your details (name, email, password).
- Description:
  - Secure authentication is handled by Firebase Authentication.

### Scope Management
- Add a Scope:
  - After logging in, you’ll land on the Scope List.
  - Tap the floating Add Scope button on the dashboard.
  - Fill in details such as scope name, brand, magnification, and zeroing settings.
  - Tap Save to add the scope.
  - Use the Navigation Drawer (accessible via the hamburger icon) to explore different sections.
- Edit/Delete a Scope:
  - Tap on the info icon of the scope you wish to see details of.
  - Swipe right on a scope to delete it. This will modify the list and remove the scope.
  - Apply the mileage filter to see what scopes you have sighted in between 1-50 miles of your current location
- Description:
  - The scope serves as the central hub, displaying saved scopes and navigation options.Manage scope data efficiently with easy-to-use forms and options.

### Weather Information
- Steps:
  - Navigate to the Weather section from the navigation drawer.
  - Enter a city/ZIP code or use your current location for real-time weather data.
- Description:
  - Fetches weather data using the OpenWeather API for informed shooting plans

### Map with Markers
- Steps:
  - Open the Map section via the navigation drawer.
  - View markers representing your location and nearby shooting ranges.
  - Tap markers for additional details.
- Description:
  - Integrates Google Maps API for location-based functionalities.

### Profile Management
- Steps:
  - Access the Profile Settings from the navigation drawer.
  - Update your username, address, or upload a profile picture.
  - Save changes to reflect updates in your account.

- Description:
  - Ensures personalized and secure user data management.

### About Section
- Steps:
  - Navigate to the About section via the navigation drawer.
  - View information about the app, developers, and licenses.
- Description:
  - Provides transparency and additional information for users.


## Contributing to ScopeHUD

### Coments and Documentation
- For documentation and comments, it is strongly encouraged to limit line length to 100 characters! Lines that are too long become difficult for users to read.

### GitHub Usage
/master - This is always a frozen snapshot of the current full release! Changes should generally not be added directly here, but could potentially include high-priority documentation fixes.

/develop - This is where your PRs should target! This branch should always compile, and code submitted here should be at least partially functional.

/feature/ - For devs working within the repository, larger in-progress features should be worked on in feature branches.


### Versioning

MAJOR.MINOR.PATCH

MAJOR - This is currently 0! MINOR - If this value increments, this indicates breaking changes PATCH - This indicates new features and bugfixes

Postfix - Preview builds look like -preview.2, we often ship multiple of these between PATCH releases. These often contain bugfixes, and will contain new features with potentially unstable APIs.