# SWENG888 - Final Project: Scope Zeroing App

## Authors:

- Andrew Young
- Roberto Luna
- André Medeiros

## Dependencies:

- Java 17

## Build Process:

    - gradlew assembleDebug

## Functional Requirements expressed as User Stories (US)

|**User Story ID**|	**User Story Description**|	**Acceptance Criteria**|
|:-------------:|:-----------------------|:--------------------|
|US-1|As a user, I want to be able to create an account in the application, so that I can securely access the features and functionalities of the app.	| **Given** that the user is on the application registration page, When the user provides valid login redentials and submits the form, Then the system should create a new account for the user and redirect them to the login page. |
|US-2|As a user, I want to be able to login and log out of the application, so that I can securely access my data and ensure privacy.| **Given** that the user is on the login page, When the user provides valid login credentials and clicks the login button, Then the system should authenticate the user and redirect them to the home page. **Given** that the user is logged in, When the user clicks the logout button, Then the system should log the user out and redirect them to the login page.
|US-3|As a user, I want to be able to view a list of items, so that I can choose which one to interact with.| **Given** that the user is on the home page, When the user clicks on the "List of Items" button, Then the system should display a list of items that the user can interact with.
|US-4|As a user, I want to be able to add an item to a list, so that I can keep track of my interactions with it.|**Given** that the user is on the list of items page, When the user clicks on the "Add Item" button and provides valid details, Then the system should add the new item to the list.|
|US-5|As a user, I want to be able to edit and delete an item from the list, so that I can update my interactions with it.| **Given** that the user is on the list of items page, When the user clicks on the "Edit" button next to an item and provides valid details, Then the system should update the item details in the list. **Given** that the user is on the list of items page, When the user clicks on the "Delete" button next to an item, Then the system should remove the item from the list.
|US-6|As a user, I want to be able to view a map with markers representing the location of items in the list, so that I can see where they are located.|**Given** that the user is on the list of items page, When the user clicks on the "View on Map" button next to an item, Then the system should display a map with a marker representing the location of the selected item.|
|US-7|As a user, I want to be able to filter the list based on location, so that I can see which items are nearby.|**Given** that the user is on the list of items page, When the user selects a location filter from the dropdown menu, Then the system should display only the items that are located within the selected area.|

## Technical Requirements:

- [x] The application should be developed using Android Studio.
- [ ] The application should have a responsive UI and be able to adapt to different screen sizes and orientations.
- [x] The application should use Firebase Authentication to handle user login and registration.
- [ ] The application should use a database to store the user data. Firebase Realtime Database or other databases such as SQLite or Room can be used.
- [x] The application should use Shared Preferences to store user preferences and settings.
- [ ] The application should incorporate the Google Maps API to display the location of items on a map.
- [x] The application should use a RecyclerView to display the list of items.

## Stretch Goals:

- [ ] Social Media Integration: Allow users to share content on social media platforms like Facebook or Twitter.
- [ ] Push Notifications: Implement push notifications to notify users about new updates or events.
- [ ] Search Functionality: Add a search bar to allow users to search for specific items or content within the app.
- [ ] User Ratings and Reviews: Add the ability for users to rate and review items or content within the app.
- [ ] Multi-language Support: Add support for multiple languages to make the app accessible to a wider audience.
- [ ] Accessibility: Make the app accessible to users with disabilities by implementing features like voiceover support and high-contrast mode.
- [ ] Machine Learning: Implement machine learning algorithms to personalize the user experience based on user behavior and preferences.
