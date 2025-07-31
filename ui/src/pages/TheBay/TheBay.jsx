import React, { useEffect, useState } from 'react';
import { fetchBay } from '../../components/Services/bayService';
import Card from '../../components/TechCards/Card';
import './TheBay.css';



const dummyData = [
  {
    id: 1,
    techName: "React",
    description: "New concurrent features and improved performance. Added automatic batching for better state updates and new hooks for concurrent rendering."
  },
  {
    id: 2,
    techName: "Spring Boot",
    description: "Enhanced security features, improved startup performance, and better integration with cloud-native applications. Added new actuator endpoints."
  },
  {
    id: 3,
    techName: "Node.js",
    description: "Performance improvements in V8 engine, new experimental features for ES modules, and enhanced debugging capabilities."
  },
  {
    id: 4,
    techName: "TypeScript",
    description: "Better type inference, improved error messages, and new utility types. Enhanced support for decorators and template literal types."
  }
];

//Return title "The Bay"

//loop through selected tech cards

//for each tech card, render a Card component

// Card.forEach(livePatchNote => {
//   <Card key={livePatchNote.id} livePatchNote={livePatchNote} />
// });

const TheBay = () => {
  const [selectedTech, setSelectedTech] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);  

  //TODO: once you have user authentication, pass the userId to fetchBay
  //TODO: implement error handling and loading state so you can better manage errors.


  // useEffect(() => {
  //   const fetchData = async () => {
  //     const data = await fetchBay();
  //     setSelectedTech(data);
  //   };

  //   fetchData();
  // }, []);

useEffect(() => {
    const fetchData = async () => {
      try {
        // Simulate API delay
        await new Promise(resolve => setTimeout(resolve, 1000));
        
        // Use dummy data instead of API call
        setSelectedTech(dummyData);
        
        // Uncomment this when your backend is ready:
        // const data = await fetchBay(1);
        // setSelectedTech(data);
        
      } catch (err) {
        setError(err.message);
        console.error('Error:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  if (loading) return <div>Loading The Bay...</div>;
  if (error) return <div>Error: {error}</div>;





  return (
    <div className="theBay">
      <h1>The Bay</h1>
      <div className="card-container">
        {selectedTech.map(livePatchNote => (
          <Card key={livePatchNote.id} livePatchNote={livePatchNote} />
        ))}
      </div>
    </div>
  );
};


export default TheBay;