# Frontend React Application

This is the frontend part of the React and Spring Boot application. Below are the instructions for setting up and running the frontend.

## Prerequisites

Make sure you have the following installed:

- Node.js (version 14 or higher)
- npm (Node Package Manager)

## Setup

1. Navigate to the `frontend` directory:

   ```bash
   cd frontend
   ```

2. Install the dependencies:

   ```bash
   npm install
   ```

## Running the Application

To start the development server, run:

```bash
npm start
```

This will start the application in development mode. Open [http://localhost:3000](http://localhost:3000) to view it in your browser.

## Building for Production

To create a production build, run:

```bash
npm run build
```

This will create an optimized build of the application in the `build` folder.

## Folder Structure

- `public/`: Contains the static files, including `index.html`.
- `src/`: Contains the React components and application logic.
  - `components/`: Contains reusable components.
  - `api/`: Contains API call functions.
  - `styles/`: Contains CSS styles for the application.

## API Integration

The frontend communicates with the backend Spring Boot application through the API functions defined in the `src/api/api.js` file. Make sure the backend is running to test the API calls.

## License

This project is licensed under the MIT License.