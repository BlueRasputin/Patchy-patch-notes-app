import React, { useEffect, useState } from 'react';
import { fetchBay } from '../../components/Services/techService';
import Card from '../../components/Cards/Card';
import './TheBay.css';


//Return title "The Bay"

//loop through selected tech cards

//for each tech card, render a Card component

Card.forEach(livePatchNote => {
  <Card key={livePatchNote.id} livePatchNote={livePatchNote} />
});