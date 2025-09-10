import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import ConnectionListDropdown from "./component/page/deviceList/ConnectionListDropdown.tsx";

function App() {
    const router = createBrowserRouter([
        {
            path: "/",
            element: <ConnectionListDropdown connectionList = {["List 1", "List 2", "List 3"]}/>
        }
    ]);
    return <RouterProvider router={router} />;
}

export default App