# SWENG888 - Final Project: Scope Zeroing App

## Authors:

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
  - 

### Google Maps API:
- Display user locations and dynamically added markers for shooting ranges.
- Setup Process:
  - 

### OpenWeather API:
- Fetch real-time weather conditions based on user input or location.
- Setup Process:
  - 

## App Architecture

## Usage Guide

The Dialed-In Zeroing App is designed to help users manage and optimize their shooting activities by allowing them to zero their scopes, access weather conditions, locate shooting ranges, and maintain a record of their settings and adjustments. This guide provides step-by-step instructions for each functionality, complete with screenshots and descriptions.

1. Login/Signup
2. Scope Management
3. Weather Information
4. Map with Markers
5. Profile Management
6. About Section


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