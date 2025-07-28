
import './App.css'

function App() {

  return (
    <>
        <meta name="viewport" content="width=device-width, initial-scale=1" />

        
          <Routes>
            <Route path="/" element={<HomePage />} />
            <Route path="The-Bay" element={<The-Bay />} />
            <Route path="/About" element={<About />} />
          </Routes>
        

    </>
  );
}

export default App;

